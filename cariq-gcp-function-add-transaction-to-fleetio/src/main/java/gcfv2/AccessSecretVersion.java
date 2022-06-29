package gcfv2;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import java.io.IOException;
import java.util.zip.CRC32C;
import java.util.zip.Checksum;


public class AccessSecretVersion {

private static final String SECRET_VERSION = Constants.SECRET_VERSION;
private static final String SECRET_NAME = Constants.SECRET_NAME;
private static final String GCP_PROJECT_NAME = Constants.GCP_PROJECT_NAME;

public static String accessSecretVersion() throws IOException {
  
    String projectId = GCP_PROJECT_NAME; //project id
    String secretId = SECRET_NAME; //your-secret-id";
    String versionId = SECRET_VERSION; //version
    return accessSecretVersion(projectId, secretId, versionId);
    //return getSecret(projectId, secretId);
  }

  public static String getSecret(String projectId, String secretId) throws IOException {
	    // Initialize client that will be used to send requests. This client only needs to be created
	    // once, and can be reused for multiple requests. After completing all of your requests, call
	    // the "close" method on the client to safely clean up any remaining background resources.
	    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
	      // Build the name.
	      SecretName secretName = SecretName.of(projectId, secretId);

	      // Create the secret.
	      Secret secret = client.getSecret(secretName);

	      // Get the replication policy.
	      String replication = "";
	      if (secret.getReplication().getAutomatic() != null) {
	        replication = "AUTOMATIC";
	      } else if (secret.getReplication().getUserManaged() != null) {
	        replication = "MANAGED";
	      } else {
	        throw new IllegalStateException("Unknown replication type");
	      }

	      System.out.printf("Secret %s, replication %s\n", secret.getName(), replication);
	      
	      return secret.getName();
	    }
	  }

  // Access the payload for the given secret version if one exists. The version
  // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
  public static String accessSecretVersion(String projectId, String secretId, String versionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      // Verify checksum. The used library is available in Java 9+.
      // If using Java 8, you may use the following:
      // https://github.com/google/guava/blob/e62d6a0456420d295089a9c319b7593a3eae4a83/guava/src/com/google/common/hash/Hashing.java#L395
      byte[] data = response.getPayload().getData().toByteArray();
      Checksum checksum = new CRC32C();
      checksum.update(data, 0, data.length);
      if (response.getPayload().getDataCrc32C() != checksum.getValue()) {
        System.out.printf("Data corruption detected.");
        return null;
      }

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String payload = response.getPayload().getData().toStringUtf8();
       //System.out.printf("Plaintext: %s\n", payload);
      return payload;
     
    }
  }
}