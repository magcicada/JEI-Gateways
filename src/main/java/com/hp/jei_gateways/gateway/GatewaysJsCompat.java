package com.hp.jei_gateways.gateway;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Method;

final class GatewaysJsCompat {
    private static final String MANAGER_CLASS_NAME = "com.hp.gatewaysjs.util.DynamicGatewayManager";
    private static final Method GET_TOOLTIP_KEY = findMethod("getTooltipKey");
    private static final Method GET_TOOLTIP_TEXT = findMethod("getTooltipText");

    private GatewaysJsCompat() {
    }

    static String getTooltipKey(ResourceLocation gatewayId) {
        return invokeString(GET_TOOLTIP_KEY, gatewayId);
    }

    static String getTooltipText(ResourceLocation gatewayId) {
        return invokeString(GET_TOOLTIP_TEXT, gatewayId);
    }

    private static Method findMethod(String methodName) {
        try {
            Class<?> clazz = Class.forName(MANAGER_CLASS_NAME);
            return clazz.getMethod(methodName, ResourceLocation.class);
        }
        catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String invokeString(Method method, ResourceLocation gatewayId) {
        if (method == null || gatewayId == null) {
            return null;
        }

        try {
            Object value = method.invoke(null, gatewayId);
            return value instanceof String string && !string.isBlank() ? string : null;
        }
        catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
