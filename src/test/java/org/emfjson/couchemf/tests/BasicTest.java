package org.emfjson.couchemf.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.emfjson.couchemf.client.CouchClient;
import org.emfjson.couchemf.client.CouchDocument;
import org.emfjson.couchemf.client.DB;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class BasicTest {

	@Test
	public void testConnectionToDefaultServer() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.Builder().build();

		assertTrue(client.isConnected());
	}

	public void testConnectionToFakeServer() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.
				Builder().
				url("http://127.0.0.1:1334").
				build();

		assertFalse(client.isConnected());
	}

	@Test
	public void testRetrieveAllDbs() throws IOException {
		CouchClient client = new CouchClient.Builder().build();

		JsonNode node = client.dbs();
		assertNotNull(node);
		assertTrue(node.isArray());
	}

	@Test
	public void testRetrieveDbInfo() throws IOException {
		CouchClient client = new CouchClient.Builder().build();
		DB db = client.db("sample");

		assertFalse(db.exist());
		db.create();
		assertTrue(db.exist());

		JsonNode node = db.info();
		assertNotNull(node);
		assertTrue(node.isObject());
		assertTrue(node.has("db_name"));
		assertEquals("sample", node.get("db_name").asText());

		db.delete();
	}

	@Test
	public void testConnectNonExistingDatabaseException() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.Builder().build();
		DB db = client.db("fake");
		JsonNode result = db.info();
		
		assertTrue(result.has("error"));
	}

	@Test
	public void testConnectNonExistingDatabase() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.Builder().build();

		assertFalse(client.hasDatabase("fake"));
	}

	@Test
	public void testCreateAndDeleteDatabase() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.Builder().build();
		assertFalse(client.hasDatabase("fake"));

		DB db = client.db("fake");
		
		JsonNode result = db.create();
		assertTrue(result.has("ok"));
		assertEquals("true", result.get("ok").asText());

		assertTrue(client.hasDatabase("fake"));
		assertTrue(db.exist());

		db.delete();
		assertFalse(client.hasDatabase("fake"));
		assertFalse(db.exist());
	}

	@Test
	public void testCreateAndDeleteDocument() throws JsonProcessingException, IOException {
		CouchClient client = new CouchClient.Builder().build();
		assertFalse(client.hasDatabase("fake"));

		DB db = client.db("fake");
		db.create();
		assertTrue(db.exist());

		String data = "{\"hello\":\"world\"}";

		CouchDocument doc = db.doc("test");
		JsonNode result = doc.create(data);

		assertTrue(result.has("ok"));
		assertTrue(result.get("ok").asBoolean());
		assertTrue(result.has("rev"));

		String rev = result.get("rev").asText();

		result = doc.delete(rev);		
		assertTrue(result.has("ok"));
		assertTrue(result.get("ok").asBoolean());

		db.delete();
		assertFalse(client.hasDatabase("fake"));
	}

}
