package org.quickbitehub.app;

import org.junit.jupiter.api.Test;
import org.quickbitehub.authentication.AuthenticationController;
import org.quickbitehub.authentication.AuthenticationService;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
class UpdateHandlerTest {
	@Test
	void stateUpdateHandlerAlwaysComesFirst() {
		AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());
		UpdateHandler handler = UpdateHandler.chain(
				new CommandUpdateHandler(authController),
				new CallBackQueryUpdateHandler(authController),
				new ReplyUpdateHandler(authController),
				new MessageUpdateHandler(),
				new InlineQueryUpdateHandler()
		);
		assertInstanceOf(StateUpdateHandler.class, handler, "StateUpdateHandler wasn't first in the chain");
	}

	@Test
	void updateHandlerChainPreserveValidOrder() {
		AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());
		UpdateHandler handler = UpdateHandler.chain(
				new StateUpdateHandler(),
				new CommandUpdateHandler(authController),
				new CallBackQueryUpdateHandler(authController),
				new ReplyUpdateHandler(authController),
				new MessageUpdateHandler(),
				new InlineQueryUpdateHandler()
		);
		String message = "Update Handler Chain does not Preserve Valid Order";
		assertInstanceOf(StateUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertInstanceOf(CommandUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertInstanceOf(CallBackQueryUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertInstanceOf(ReplyUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertInstanceOf(MessageUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertInstanceOf(InlineQueryUpdateHandler.class, handler, message);
		handler = handler.nextHandler;
		assertNull(handler, "Chain of Update Handlers is not ended well");
	}

	@Test
	void updateHandlerChainPreserveUniqueness() {
		AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());
		UpdateHandler handler = UpdateHandler.chain(
				new StateUpdateHandler(),
				new StateUpdateHandler(),
				new CommandUpdateHandler(authController),
				new StateUpdateHandler(),
				new ReplyUpdateHandler(authController),
				new CallBackQueryUpdateHandler(authController),
				new ReplyUpdateHandler(authController),
				new CommandUpdateHandler(authController),
				new CommandUpdateHandler(authController),
				new MessageUpdateHandler(),
				new InlineQueryUpdateHandler(),
				new MessageUpdateHandler(),
				new InlineQueryUpdateHandler()
		);

		List<Class<? extends UpdateHandler>> handlerList = new ArrayList<>(13);
		while (handler != null) {
			handlerList.add(handler.getClass());
			handler = handler.nextHandler;
		}
		Set<Class<? extends UpdateHandler>> uniqueHandlers = new HashSet<>(handlerList);
		assertEquals(6, uniqueHandlers.size());
	}

	@Test
	void messageUpdateHandlerFollowsCommandAndReplyHandlers() {
		AuthenticationController authController = AuthenticationController.initInstance(AuthenticationService.getInstance());
		UpdateHandler handler = UpdateHandler.chain(
				new StateUpdateHandler(),
				new CallBackQueryUpdateHandler(authController),
				new ReplyUpdateHandler(authController),
				new MessageUpdateHandler(),
				new CommandUpdateHandler(authController),
				new InlineQueryUpdateHandler()
		);

		boolean isMessageHandlerSeen = false;
		while (handler != null) {
			if (handler instanceof MessageUpdateHandler) isMessageHandlerSeen = true;
			if (handler instanceof CommandUpdateHandler || handler instanceof ReplyUpdateHandler) {
				assertFalse(isMessageHandlerSeen, "MessageUpdateHandler appeared before Command/Reply handler.");
			}
			handler = handler.nextHandler;
		}
	}
}