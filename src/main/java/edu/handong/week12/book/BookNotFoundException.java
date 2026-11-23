package edu.handong.week12.book;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("찾을 수 없습니다. id=" + id);
    }
}
