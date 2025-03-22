package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.ModelReference;
import com.amplifyframework.core.model.LoadedModelReferenceImpl;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.core.model.ModelIdentifier;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Notifications type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Notifications", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.UPDATE })
}, hasLazySupport = true)
@Index(name = "undefined", fields = {"id"})
public final class Notifications implements Model {
  public static final NotificationsPath rootPath = new NotificationsPath("root", false, null);
  public static final QueryField ID = field("Notifications", "id");
  public static final QueryField USER = field("Notifications", "userId");
  public static final QueryField MESSAGE = field("Notifications", "message");
  public static final QueryField READ = field("Notifications", "read");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="User") @BelongsTo(targetName = "userId", targetNames = {"userId"}, type = User.class) ModelReference<User> user;
  private final @ModelField(targetType="String", isRequired = true) String message;
  private final @ModelField(targetType="Boolean", isRequired = true) Boolean read;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  /** @deprecated This API is internal to Amplify and should not be used. */
  @Deprecated
   public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public ModelReference<User> getUser() {
      return user;
  }
  
  public String getMessage() {
      return message;
  }
  
  public Boolean getRead() {
      return read;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Notifications(String id, ModelReference<User> user, String message, Boolean read) {
    this.id = id;
    this.user = user;
    this.message = message;
    this.read = read;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Notifications notifications = (Notifications) obj;
      return ObjectsCompat.equals(getId(), notifications.getId()) &&
              ObjectsCompat.equals(getUser(), notifications.getUser()) &&
              ObjectsCompat.equals(getMessage(), notifications.getMessage()) &&
              ObjectsCompat.equals(getRead(), notifications.getRead()) &&
              ObjectsCompat.equals(getCreatedAt(), notifications.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), notifications.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUser())
      .append(getMessage())
      .append(getRead())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Notifications {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("user=" + String.valueOf(getUser()) + ", ")
      .append("message=" + String.valueOf(getMessage()) + ", ")
      .append("read=" + String.valueOf(getRead()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static MessageStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Notifications justId(String id) {
    return new Notifications(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      user,
      message,
      read);
  }
  public interface MessageStep {
    ReadStep message(String message);
  }
  

  public interface ReadStep {
    BuildStep read(Boolean read);
  }
  

  public interface BuildStep {
    Notifications build();
    BuildStep id(String id);
    BuildStep user(User user);
  }
  

  public static class Builder implements MessageStep, ReadStep, BuildStep {
    private String id;
    private String message;
    private Boolean read;
    private ModelReference<User> user;
    public Builder() {
      
    }
    
    private Builder(String id, ModelReference<User> user, String message, Boolean read) {
      this.id = id;
      this.user = user;
      this.message = message;
      this.read = read;
    }
    
    @Override
     public Notifications build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Notifications(
          id,
          user,
          message,
          read);
    }
    
    @Override
     public ReadStep message(String message) {
        Objects.requireNonNull(message);
        this.message = message;
        return this;
    }
    
    @Override
     public BuildStep read(Boolean read) {
        Objects.requireNonNull(read);
        this.read = read;
        return this;
    }
    
    @Override
     public BuildStep user(User user) {
        this.user = new LoadedModelReferenceImpl<>(user);
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, ModelReference<User> user, String message, Boolean read) {
      super(id, user, message, read);
      Objects.requireNonNull(message);
      Objects.requireNonNull(read);
    }
    
    @Override
     public CopyOfBuilder message(String message) {
      return (CopyOfBuilder) super.message(message);
    }
    
    @Override
     public CopyOfBuilder read(Boolean read) {
      return (CopyOfBuilder) super.read(read);
    }
    
    @Override
     public CopyOfBuilder user(User user) {
      return (CopyOfBuilder) super.user(user);
    }
  }
  

  public static class NotificationsIdentifier extends ModelIdentifier<Notifications> {
    private static final long serialVersionUID = 1L;
    public NotificationsIdentifier(String id) {
      super(id);
    }
  }
  
}
