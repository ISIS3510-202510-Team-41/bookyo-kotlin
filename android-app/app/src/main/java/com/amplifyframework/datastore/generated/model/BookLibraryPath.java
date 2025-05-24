package com.amplifyframework.datastore.generated.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amplifyframework.core.model.ModelPath;
import com.amplifyframework.core.model.PropertyPath;

/** This is an auto generated class representing the ModelPath for the BookLibrary type in your schema. */
public final class BookLibraryPath extends ModelPath<BookLibrary> {
  private BookPath book;
  private UserLibraryPath userLibraryRef;
  BookLibraryPath(@NonNull String name, @NonNull Boolean isCollection, @Nullable PropertyPath parent) {
    super(name, isCollection, parent, BookLibrary.class);
  }
  
  public synchronized BookPath getBook() {
    if (book == null) {
      book = new BookPath("book", false, this);
    }
    return book;
  }
  
  public synchronized UserLibraryPath getUserLibraryRef() {
    if (userLibraryRef == null) {
      userLibraryRef = new UserLibraryPath("userLibraryRef", false, this);
    }
    return userLibraryRef;
  }
}
