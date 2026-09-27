package vineet.order_management.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import vineet.order_management.model.OrderStatus;

public class OderStateValidator {
    private static final Map<OrderStatus , Set<OrderStatus>>transitions = new HashMap<>();

    static {
        transitions.put(OrderStatus.PLACED, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        transitions.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        transitions.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }
    public boolean canTransition(OrderStatus currentStatus , OrderStatus newStatus){
        Set<OrderStatus>validTransitions = transitions.get(currentStatus);
        return validTransitions != null && validTransitions.contains(newStatus);
    }

    public Set<OrderStatus> getValidTransitions(OrderStatus from){
        return Collections.unmodifiableSet(transitions.getOrDefault(from, EnumSet.noneOf(OrderStatus.class)));
    }

}
