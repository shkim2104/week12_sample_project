# Week 12 - Book REST API 고도화 및 Render 배포

 

 **GitHub Repository** https://github.com/2026-2-WebService/assign12-c01-22300133 |
 **Render Deployment**  `TODO: https://<서비스명>.onrender.com` |
 **Swagger UI (배포)**  `TODO: https://<서비스명>.onrender.com/swagger-ui.html` |
**OpenAPI JSON (배포)**  `TODO: https://<서비스명>.onrender.com/v3/api-docs` |

---

## 1. Project Information

제공된 Book REST API Sample Project(Spring Boot + JPA 기본 CRUD)를 분석하고, 12주차 강의와 Lab 1~4 내용을 적용해 REST API를 고도화한 뒤 Render에 배포한 프로젝트입니다.


### 기술 스택
- Java 17, Spring Boot 3.5.7, Spring Data JPA (Hibernate 6.6)
- MariaDB (학교 제공 DB, 10.1.48) / H2 (로컬 기본값)
- spring-boot-starter-validation, springdoc-openapi-starter-webmvc-ui 2.8.9
- Gradle 9.3 (Wrapper 포함), Docker, Render


## 2. Installation

### 요구 환경
- **Java 17** (Gradle toolchain이 17로 설정되어 있음)
- Gradle은 별도 설치 불필요 (`gradlew` Wrapper 사용)
- MariaDB (학교 제공 DB 또는 `compose.yaml`로 실행하는 로컬 Docker DB)


**② MariaDB 연결 실행**
환경 변수를 설정한 뒤 실행합니다. (IntelliJ: Run Configuration → Environment variables)

 `SPRING_PROFILES_ACTIVE`  `application-mariadb.yml` 활성화  `mariadb` 
 `DB_URL`  JDBC URL  `jdbc:mariadb://<host>:3306/<database>` 
 `DB_USERNAME`  DB 계정 
 `DB_PASSWORD`  DB 비밀번호
 `DDL_AUTO`  테이블이 없을 때 최초 1회만 `update`, 이후 `validate`  `validate` 

> 로컬 Docker DB를 쓰는 경우: `docker compose up -d` 후 `DB_URL=jdbc:mariadb://localhost:3307/week12`, `DB_USERNAME=week12`, `DB_PASSWORD=week12_local_only`

**③ 접속 확인**
- API: http://localhost:8080/api/books
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs



## 3. Source Code Exploration

#### Project Structure & JPA

**Q1. BookController, BookService, BookRepository의 역할과 요청 처리 흐름을 설명하세요.**
controller는 HTTP 요청을 받아 파라미터와 바디를 받아 Service를 부르고, 받은 결과를 응답rh으로 돌리고, Service는 비즈니스 로직을 맡아  검증, 조합, 예외 던지기가 여기서 일어나거, Repository는 DB에 접근하는 흐름입니다. 

**Q2. `JpaRepository<Book, Long>`만 선언해도 `findAll()`, `save()`를 사용할 수 있는 이유를 설명하세요.**
JpaRepository가 CrudRepository와 PagingAndSortingRepository를 상속해서 findAll, save, findById, deleteById 같은 메서드가 이미 정의되어 있기 때문에 실행할 때는 Spring Data JPA가 프록시 구현체를 자동으로 만들어 빈으로 등록하기 때문에 선언만 해도 괜찮습니다.

#### DTO & HTTP Response

**Q3. 기존 API가 Book Entity를 직접 요청·응답에 사용할 때 발생할 수 있는 문제를 설명하세요.**
노출하면 안 되는 필드값이 그대로 드러나거나, 클라이언트가 id 같은 값을 임의로 넣을 수 있고, 엔티티를 바꾸면 API 스펙도 같이 바뀌어서 결합도가 높아집니다.

**Q4. Request DTO와 Response DTO를 분리하는 이유와 ResponseEntity가 필요한 상황을 설명하세요.**
Request DTO는 클라이언트가 보낼 수 있는 필드와 검증 규칙만 담아서, Response DTO는 보여줄 필드만 가지고 갑니다. 그래서 입력과 출력 스펙을 따로 관리할 수 있고 엔티티는 보호가 됩니다.
ResponseEntity는 상태 코드나 헤더를 직접 정해야 할 때 사용하는데, 예를 들어 생성 시 201 Created와 Location 헤더, 삭제 시 204 No Content, 없는 데이터에 404, 검증 실패에 400을 줄여주는 이점이 있습니다.

#### Validation & Exception Handling

**Q5. `@Valid`, `@NotBlank`, `@Min`은 각각 어느 시점에 어떤 동작을 수행하나요?**
@NotBlank와 @Min은 DTO 필드에 붙이는 제약 조건 선언입니다. 그 자체로는 아무것도 하지 않고, @NotBlank는 null, 빈 문자열, 공백만 있는 문자열을 막고, @Min(0)은 값이 0 이상인지 확인합니다.
@Valid는 컨트롤러 파라미터(@RequestBody @Valid BookRequest)에 붙여서, 요청 JSON이 DTO로 바인딩된 직후, 메서드가 실행되기 전에 검증에 들어가서 실패하면 메서드는 실행되지 않고 MethodArgumentNotValidException이 발생합니다.

**Q6. `@RestControllerAdvice`와 `@ExceptionHandler`를 사용하면 예외 처리 흐름이 어떻게 바뀌나요?**
@restcontrolleradvice가 없으면 컨트롤러마다 try-catch를 쓰거나 기본 에러 응답이 그대로 나삽니다. @restcontrolleradvice를 쓰면 어느 컨트롤러에서 예외가 나든 전역 핸들러로 모여지고, 예외 타입별 @ExceptionHandler 메서드가 일관된 에러 JSON과 상태 코드를 만드는 덕분에 컨트롤러에는 정상 흐름만 남게 됩니다.

#### Pagination & OpenAPI

**Q7. `Pageable`과 `Page<T>`는 각각 어떤 역할을 하며 `sort=price,desc`는 어떻게 처리되나요?**
Pageable은 요청 정보입니다. page 번호, size, sort를 담고 있고, 컨트롤러 파라미터로 받으면 ?page=0&size=10&sort=price,desc가 자동으로 바인딩됩니다.
Page<T>는 결과입니다. content에 더해 totalElements, totalPages, number, size 같은 메타정보를 담아서 보여줍니다.
sort=price,desc는 Sort.by(DESC, "price")로 바뀌고, findAll(pageable)을 실행할 때 SQL에 ORDER BY price DESC와 LIMIT/OFFSET이 붙게 됩니다.

**Q8. `/v3/api-docs`와 `/swagger-ui.html`의 역할을 구분해서 설명하세요.**
/v3/api-docs는 springdoc이 컨트롤러를 스캔해서 만든 OpenAPI 명세 JSON이라서 기게어를 처리하는 컴퓨터가 읽게됩니다.
/swagger-ui.html은 그 JSON을 읽어서 사람이 보기 좋게 보여주는 웹 화면이고, 여기서 API를 바로 호출해 테스트할 수도 있습니다.

### ② Line-by-Line Code Comments

줄 단위 설명 주석을 작성한 메서드 (주석은 소스 파일에서 확인)

## 4. Refactoring (DTO & ResponseEntity)


 ![초기 CRUD](results/01_sample.png) ![변경 전 문제점](results/01_before_issues.png) 


 ![DTO 201](results/02_dto_created.png)  ![DTO CRUD](results/02_dto_crud.png) 

---

## 5. New Features

### 5-1. Validation
`BookRequest`에 Bean Validation을 적용하고, Controller의 `create()`/`update()` 파라미터에 `@Valid`를 붙였습니다.


| `title` | `@NotBlank`, `@Size(max=150)` | 제목은 필수입니다. |
| `author` | `@NotBlank`, `@Size(max=100)` | 저자는 필수입니다. |
| `price` | `@NotNull`, `@Min(0)` | 가격은 필수입니다. / 가격은 0 이상이어야 합니다. |

> `price`를 `int`가 아닌 `Integer`로 선언해 값이 누락된 경우(null)도 `@NotNull`로 검출합니다.

### 5-2. 공통 예외 처리 (`@RestControllerAdvice`)
| 예외 | 상황 | 상태 | code |
|---|---|---|---|
| `MethodArgumentNotValidException` | Validation 실패 | 400 | `VALIDATION_FAILED` |
| `HttpMessageNotReadableException` | JSON 문법 오류, 타입 불일치 | 400 | `INVALID_JSON` |
| `MethodArgumentTypeMismatchException` | `/api/books/abc` | 400 | `INVALID_PARAMETER` |
| `PropertyReferenceException` | `sort=foo` (없는 필드) | 400 | `INVALID_SORT` |
| `BookNotFoundException` | 없는 ID 조회·수정·삭제 | 404 | `BOOK_NOT_FOUND` |

공통 오류 응답 형식:
```json
{
  "code": "VALIDATION_FAILED",
  "message": "입력값이 올바르지 않습니다.",
  "errors": [ { "field": "price", "reason": "가격은 0 이상이어야 합니다." } ],
  "timestamp": "2026-11-23T18:37:07.408"
}
```

### 5-3. Pagination / Sorting
- `GET /api/books?page=0&size=5&sort=price,desc`
- 기본값: `@PageableDefault(size = 5, sort = "id", direction = ASC)`, 최대 페이지 크기 50 (`spring.data.web.pageable.max-page-size`)
- `Page<Book>.map(BookResponse::from)`으로 페이징 메타데이터는 유지하고 content만 DTO로 변환
- `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)`로 응답 구조를 `{content, page}`로 고정
- 다중 정렬 지원: `sort=author,asc&sort=price,desc`

### 5-4. Swagger / OpenAPI
- Swagger UI: `/swagger-ui.html`, OpenAPI JSON: `/v3/api-docs`
- `@Tag(name = "Book API")`, 5개 API 모두 `@Operation(summary, description)`, `@ApiResponse`로 응답 코드 문서화
- `@ParameterObject`로 `Pageable`을 page / size / sort 입력 칸으로 분리 표시
- `OpenApiConfig`에서 문서 제목·설명·버전 설정

---

## 6. Testing & Deployment

#### 03. Validation (400)
![Validation](results/03_validation.png)
![Invalid JSON](results/03_invalid_json.png)

#### 04. Not Found (404)
![Not Found](results/04_not_found.png)
![CRUD OK](results/04_crud_ok.png)

#### 05. Pagination / Sorting
![Pagination](results/05_pagination.png)
![Pagination More](results/05_pagination_more.png)

#### 06. Swagger UI / OpenAPI
![Swagger](results/06_swagger.png)
![Swagger Try](results/06_swagger_try.png)
![API Docs](results/06_api_docs.png)

#### 07. 학교 MariaDB 연결 확인
![MariaDB CRUD](results/07_mariadb_crud.png)
![MariaDB Paging](results/07_mariadb_paging.png)
<!-- TODO: DBeaver 캡처 추가 후 주석 해제
![MariaDB Table](results/07_mariadb_table.png)
-->

### 6-2. GitHub & Render Deployment

**배포 방식:** Render는 Java 런타임을 직접 지원하지 않아 `Dockerfile`로 배포합니다.
1. 1단계(`eclipse-temurin:17-jdk`)에서 `./gradlew bootJar`로 `app.jar` 빌드
2. 2단계(`eclipse-temurin:17-jre`)에서 `java -jar app.jar` 실행
3. 서버 포트는 Render가 넣어주는 `PORT` 환경 변수를 `application.yml`의 `server.port: ${PORT:8080}`으로 사용

**Render 환경 변수** (DB 비밀번호는 Render 대시보드에만 입력, GitHub에는 올리지 않음)

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `mariadb` |
| `DB_URL` | `jdbc:mariadb://<host>:3306/<database>` |
| `DB_USERNAME` | (비공개) |
| `DB_PASSWORD` | (비공개) |
| `DDL_AUTO` | `validate` |

**배포 확인**
- API: `TODO: https://<서비스명>.onrender.com/api/books`
- Swagger UI: `TODO: https://<서비스명>.onrender.com/swagger-ui.html`


### 6-3. 실습 환경 문제 및 조치 내역

| # | 단계 | 현상 / 오류 메시지 | 원인 | 조치 |
|---|---|---|---|---|
| 1 | 학교 MariaDB 연결 | GET은 200, POST만 500<br>`SQL Error: 1364, SQLState: HY000`<br>`Field 'category' doesn't have a default value`<br>`[insert into books (author,price,title) values (?,?,?)]` | 학교 DB에 이전 실습에서 만든 같은 이름의 `books` 테이블이 있었고, 그 테이블에 NOT NULL 컬럼 `category`가 존재. `ddl-auto=update`는 기존 컬럼을 삭제·변경하지 않음 | 기존 테이블을 삭제하지 않고 `@Table(name = "week12_books")`로 이번 과제 전용 테이블을 분리 |
| 2 | 학교 MariaDB 연결 | `HHH000511: The 10.1.48 version for [org.hibernate.dialect.MariaDBDialect] is no longer supported ... The minimum supported version is 10.4.0` | 학교 DB 버전(MariaDB 10.1.48)이 Hibernate 6.6의 최소 지원 버전(10.4)보다 낮음 | 경고만 발생하고 CRUD·페이징은 정상 동작함을 확인 (`07_mariadb_*.png`) |
| 3 | Render 배포 | TODO: 배포 중 발생한 오류가 있으면 메시지 원문 기록 | | |

---

