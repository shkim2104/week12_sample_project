package edu.handong.week12.book;

import edu.handong.week12.book.dto.BookRequest;
import edu.handong.week12.book.dto.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {
  private final BookRepository repository;

  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  public Page<BookResponse> findAll(Pageable pageable) {
    Page<Book> books = repository.findAll(pageable);
    return books.map(BookResponse::from);
  }

  public BookResponse findById(Long id) {
    Book book = repository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));
    return BookResponse.from(book);
  }

  @Transactional
  public BookResponse create(BookRequest request) {
    Book book = new Book(request.title(), request.author(), request.price());
    Book saved = repository.save(book);
    return BookResponse.from(saved);
  }

  @Transactional
  public BookResponse update(Long id, BookRequest request) {
    Book book = repository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));
    book.update(request.title(), request.author(), request.price());
    return BookResponse.from(book);
  }

  @Transactional
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new BookNotFoundException(id);
    }
    repository.deleteById(id);
  }
}
