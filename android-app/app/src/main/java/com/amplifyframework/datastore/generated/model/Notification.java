package com.amplifyframework.datastore.generated.model;

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

/** This is an auto generated class representing the Notification type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Notifications", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ, ModelOperation.CREATE, ModelOperation.DELETE })
}, hasLazySupport = true)
@Index(name = "undefined", fields = {"id"})
public final class Notification implements Model {
  public static final NotificationPath rootPath = new NotificationPath("root", false, null);
  public static final QueryField ID = field("Notification", "id");
  public static final QueryField TITLE = field("Notification", "title");
  public static final QueryField BODY = field("Notification", "body");
  public static final QueryField RECIPIENT = field("Notification", "recipient");
  public static final QueryField READ = field("Notification", "read");
  public static final QueryField TYPE = field("Notification", "type");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String title;
  private final @ModelField(targetType="String", isRequired = true) String body;
  private final @ModelField(targetType="String", isRequired = true) String recipient;
  private final @ModelField(targetType="Boolean", isRequired = true) Boolean read;
  private final @ModelField(targetType="NotificationType") NotificationType type;
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
  
  public String getTitle() {
      return title;
  }
  
  public String getBody() {
      return body;
  }
  
  public String getRecipient() {
      return recipient;
  }
  
  public Boolean getRead() {
      return read;
  }
  
  public NotificationType getType() {
      return type;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Notification(String id, String title, String body, String recipient, Boolean read, NotificationType type) {
    this.id = id;
    this.title = title;
    this.body = body;
    this.recipient = recipient;
    this.read = read;
    this.type = type;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Notification notification = (Notification) obj;
      return ObjectsCompat.equals(getId(), notification.getId()) &&
              ObjectsCompat.equals(getTitle(), notification.getTitle()) &&
              ObjectsCompat.equals(getBody(), notification.getBody()) &&
              ObjectsCompat.equals(getRecipient(), notification.getRecipient()) &&
              ObjectsCompat.equals(getRead(), notification.getRead()) &&
              ObjectsCompat.equals(getType(), notification.getType()) &&
              ObjectsCompat.equals(getCreatedAt(), notification.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), notification.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getBody())
      .append(getRecipient())
      .append(getRead())
      .append(getType())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Notification {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("body=" + String.valueOf(getBody()) + ", ")
      .append("recipient=" + String.valueOf(getRecipient()) + ", ")
      .append("read=" + String.valueOf(getRead()) + ", ")
      .append("type=" + String.valueOf(getType()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static TitleStep builder() {
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
  public static Notification justId(String id) {
    return new Notification(
      id,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      body,
      recipient,
      read,
      type);
  }
  public interface TitleStep {
    BodyStep title(String title);
  }
  

  public interface BodyStep {
    RecipientStep body(String body);
  }
  

  public interface RecipientStep {
    ReadStep recipient(String recipient);
  }
  

  public interface ReadStep {
    BuildStep read(Boolean read);
  }
  

  public interface BuildStep {
    Notification build();
    BuildStep id(String id);
    BuildStep type(NotificationType type);
  }
  

  public static class Builder implements TitleStep, BodyStep, RecipientStep, ReadStep, BuildStep {
    private String id;
    private String title;
    private String body;
    private String recipient;
    private Boolean read;
    private NotificationType type;
    public Builder() {
      
    }
    
    private Builder(String id, String title, String body, String recipient, Boolean read, NotificationType type) {
      this.id = id;
      this.title = title;
      this.body = body;
      this.recipient = recipient;
      this.read = read;
      this.type = type;
    }
    
    @Override
     public Notification build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Notification(
          id,
          title,
          body,
          recipient,
          read,
          type);
    }
    
    @Override
     public BodyStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public RecipientStep body(String body) {
        Objects.requireNonNull(body);
        this.body = body;
        return this;
    }
    
    @Override
     public ReadStep recipient(String recipient) {
        Objects.requireNonNull(recipient);
        this.recipient = recipient;
        return this;
    }
    
    @Override
     public BuildStep read(Boolean read) {
        Objects.requireNonNull(read);
        this.read = read;
        return this;
    }
    
    @Override
     public BuildStep type(NotificationType type) {
        this.type = type;
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
    private CopyOfBuilder(String id, String title, String body, String recipient, Boolean read, NotificationType type) {
      super(id, title, body, recipient, read, type);
      Objects.requireNonNull(title);
      Objects.requireNonNull(body);
      Objects.requireNonNull(recipient);
      Objects.requireNonNull(read);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder body(String body) {
      return (CopyOfBuilder) super.body(body);
    }
    
    @Override
     public CopyOfBuilder recipient(String recipient) {
      return (CopyOfBuilder) super.recipient(recipient);
    }
    
    @Override
     public CopyOfBuilder read(Boolean read) {
      return (CopyOfBuilder) super.read(read);
    }
    
    @Override
     public CopyOfBuilder type(NotificationType type) {
      return (CopyOfBuilder) super.type(type);
    }
  }
  

  public static class NotificationIdentifier extends ModelIdentifier<Notification> {
    private static final long serialVersionUID = 1L;
    public NotificationIdentifier(String id) {
      super(id);
    }
  }
  
}
