package bloggingplatform;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class BloggingPlatform {
    public static Scanner scanner = new Scanner(System.in);
    public static boolean running = true;
    public static MongoDatabase database;
    public static MongoCollection<Document> postsCollection;
    public static String userName;

    public static void main (String[] args) {
        // MongoDB setup
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        // Get database and connection
        database = mongoClient.getDatabase("bloggingplatform");
        postsCollection = database.getCollection("posts");

        System.out.println("Enter your name: ");
        userName = scanner.nextLine();

        while (running) {
            System.out.println("Blogging Platform");
            System.out.println("1. View Posts");
            System.out.println("2. Create Posts");
            System.out.println("3. Update Post");
            System.out.println("4. Delete Post");
            System.out.println("5. Comment on Post");
            System.out.println("6. View Comments");
            System.out.println("7. Search Posts by Tags");
            System.out.println("8. View Posts Sorted by Date");
            System.out.println("9. End");

            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 1) {
                // View all posts
                viewPosts();
            } else if (choice == 2) {
                // Create a new post
                createPost();
            } else if (choice == 3) {
                // Update a post by its id
                updatePost();
            } else if (choice == 4) {
                // Delete post filter by ID
                deletePosts();
            } else if (choice == 5) {
                // Add comment to post by title
                addComment();
            } else if (choice == 6) {
                // View all comments on a post by title
                viewComments();
            }else if (choice == 7) {
                // View all comments on a post by title
                searchPostsByTag();
            }else if (choice == 8) {
                // View all comments on a post by title
                viewPostsSortedByDate();
            }else if (choice == 9) {
                running = false;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void createPost () {
        System.out.println("Create Post");
        System.out.println("Title: ");
        String title = scanner.nextLine();
        System.out.println("Description: ");
        String description = scanner.nextLine();
        System.out.println("Author: ");
        String author = scanner.nextLine();
        System.out.println("Tag: ");
        String tag = scanner.nextLine();

        // Create a post, add data
        Document document = new Document()
                .append("title", title)
                .append("description", description)
                .append("author", author)
                .append("tag", tag)
                .append("comments", new ArrayList<Document>())
                .append("date", new Date());


        postsCollection.insertOne(document);
        System.out.println("Document inserted successfully!");
    }

    public static void updatePost() {
        System.out.println("Post ID to update: ");
        String postId = scanner.nextLine();
        ObjectId postIdUpdate = new ObjectId(postId);

        // Find the post to update using ID
        Document post = postsCollection.find(new Document("_id", postIdUpdate)).first();
        System.out.println(post);
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        // Only allows you to update if you are the creator of the post
        String author = post.getString("author");
        if (!userName.equals(author)) {
            System.out.println("You can only update your own posts.");
            return;
        }

        System.out.println("New Title: ");
        String title = scanner.nextLine();
        System.out.println("New Description: ");
        String description = scanner.nextLine();

        // Updates the document with ID
        Document updateFilter = new Document("_id", postIdUpdate);
        Document update = new Document("$set", new Document("title", title).append("description", description));
        postsCollection.updateOne(updateFilter, update);
        System.out.println("Document updated successfully!");
    }

    public static void viewPosts() {
        // Prints all the posts in the collection
        for (Document doc : postsCollection.find()) {
            System.out.println(doc.toJson());
        }
    }

    public static void deletePosts() {
        System.out.println("Post ID to delete: ");
        String postId = scanner.nextLine();
        ObjectId postIdDelete = new ObjectId(postId);

        Document post = postsCollection.find(new Document("_id", postIdDelete)).first();
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        // Checks if logged-in user is the creator of post
        String author = post.getString("author");
        if (!userName.equals(author)) {
            System.out.println("You can only update your own posts.");
            return;
        }

        // Delete post by ID
        Document deletePost = new Document("_id", postIdDelete);
        postsCollection.deleteOne(deletePost);
        System.out.print("Post deleted successfully.");
    }

    public static void addComment() {
        System.out.println("Post title to comment on: ");
        String title = scanner.nextLine();
        System.out.print("Comment Author: ");
        String commentAuthor = scanner.nextLine();
        System.out.print("Comment Text: ");
        String commentText = scanner.nextLine();

        // Insert author, text into comment array in post collection
        Document comment = new Document()
                .append("author", commentAuthor)
                .append("text", commentText);
        Document commentFilter = new Document("title", title);
        Document commentUpdate = new Document("$push", new Document("comments", comment));
        postsCollection.updateOne(commentFilter,commentUpdate);
        System.out.print("Post comment successful.");
    }

    public static void viewComments() {
        System.out.println("Enter Post Title: ");
        String postTitle = scanner.nextLine();

        Document postToViewComments = postsCollection.find(new Document("title", postTitle)).first();

        // Prints all comments of specific post by title
        if (postToViewComments != null) {
            ArrayList<Document> comments = (ArrayList<Document>) postToViewComments.get("comments");
            if (comments != null && !comments.isEmpty()) {
                for (Document commentDoc : comments) {
                    System.out.println("Author: " + commentDoc.getString("author"));
                    System.out.println("Text: " + commentDoc.getString("text"));
                    System.out.println("-----------------------------");
                }
            } else {
                System.out.println("No comments found.");
            }
        } else {
            System.out.println("Post does not exist.");
        }
    }

    public static void searchPostsByTag() {
        System.out.println("Enter tag to search: ");
        String tag = scanner.nextLine();

        // Search posts based on tag
        for (Document doc : postsCollection.find(eq("tag", tag))) {
            System.out.println(doc.toJson());
        }
    }

    public static void viewPostsSortedByDate() {
        // View posts by desc order
        for (Document doc : postsCollection.find().sort(descending("date"))) {
            System.out.println(doc.toJson());
        }
    }

}
