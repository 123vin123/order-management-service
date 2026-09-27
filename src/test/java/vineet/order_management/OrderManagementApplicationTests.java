package vineet.order_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import vineet.order_management.controller.AuthController;
import vineet.order_management.model.Account;
import vineet.order_management.model.CatalogProduct;
import vineet.order_management.service.AccountService;
import vineet.order_management.service.OrderService;

@SpringBootTest
class OrderManagementApplicationTests {
	@Autowired
	private AccountService accountService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void contextLoads() {
	}

	@Test
	void storesHashedAccountAndPersistsBookingWithReservedStock() {
		String password = "safe-password-123";
		Account account = accountService.register("Test Customer", UUID.randomUUID() + "@example.com", password);
		assertNotEquals(password, account.getPasswordHash());
		assertTrue(accountService.authenticate(account.getEmail(), password).getId().equals(account.getId()));

		CatalogProduct product = orderService.getProducts().get(0);
		int initialStock = product.getStock();
		var booking = orderService.placeOrder(account, product.getId(), 2);

		assertEquals(2, booking.getQuantity());
		assertEquals(1, orderService.getOrders(account.getId()).size());
		assertEquals(initialStock - 2, orderService.getProducts().stream()
				.filter(item -> item.getId().equals(product.getId())).findFirst().orElseThrow().getStock());
	}

	@Test
	void authenticatedCustomerCanBookAndOnlyTheirSessionCanViewBookings() throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		String email = UUID.randomUUID() + "@example.com";
		MvcResult registration = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthController.Credentials("API Customer", email,
						"safe-password-456", false))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.authenticated").value(true))
				.andReturn();
		MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);

		JsonNode product = objectMapper.readTree(mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get(0);
		mockMvc.perform(post("/api/bookings").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":" + product.get("id").asLong() + ",\"quantity\":1}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.reference").exists())
				.andExpect(jsonPath("$.emailSent").value(false));
		mockMvc.perform(get("/api/bookings").session(session))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].productName").exists());
		mockMvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());
		mockMvc.perform(get("/api/bookings").session(session)).andExpect(status().isUnauthorized());
		MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AuthController.Credentials(null, email,
						"safe-password-456", true))))
				.andExpect(status().isOk()).andReturn();
		MockHttpSession rememberedSession = (MockHttpSession) login.getRequest().getSession(false);
		assertEquals(14 * 24 * 60 * 60, rememberedSession.getMaxInactiveInterval());
		mockMvc.perform(get("/api/bookings").session(rememberedSession)).andExpect(status().isOk());
	}

}
