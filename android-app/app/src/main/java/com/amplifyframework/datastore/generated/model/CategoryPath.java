package com.amplifyframework.datastore.generated.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amplifyframework.core.model.ModelPath;
import com.amplifyframework.core.model.PropertyPath;

/** This is an auto generated class representing the ModelPath for the Category type in your schema. */
public final class CategoryPath extends ModelPath<Category> {
  private BookCategoryPath books;
  CategoryPath(@NonNull String name, @NonNull Boolean isCollection, @Nullable PropertyPath parent) {
    super(name, isCollection, parent, Category.class);
  }
  
  public synchronized BookCategoryPath getBooks() {
    if (books == null) {
      books = new BookCategoryPath("books", true, this);
    }
    return books;
  }
}
