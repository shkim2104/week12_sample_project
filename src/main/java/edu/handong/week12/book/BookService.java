package edu.handong.week12.book;

import edu.handong.week12.book.dto.BookRequest;
import edu.handong.week12.book.dto.BookResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {
  private final BookRepository repository;

  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  public List<BookResponse> findAll() {
    return repository.findAll().stream()
        .map(BookResponse::from)
        .toList();
  }

  public BookResponse findById(Long id) {
    Book book = repository.findById(id).orElseThrow();
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
    Book book = repository.findById(id).orElseThrow();
    book.update(request.title(), request.author(), request.price());
    return BookResponse.from(book);
  }

  @Transactional
  public void delete(Long id) {
    repository.deleteById(id);
  }
}
