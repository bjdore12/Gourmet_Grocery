import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Stack;

public class Scheduler {
    // TODO: The scheduler will need to be able to be able to assign delivery personnel and delivery dates to an order.
    final private static LocalTime DELIVERY_TIME_ONE = LocalTime.of(9,00);
    final private static LocalTime DELIVERY_TIME_TWO = LocalTime.of(11,00);
    final private static LocalTime DELIVERY_TIME_THREE = LocalTime.of(14,00);
    final private static LocalTime DELIVERY_TIME_FOUR = LocalTime.of(16, 00);

    private static int timeOneSlots = 2;
    private static int timeTwoSlots = 2;
    private static int timeThreeSlots = 2;
    private static int timeFourSlots = 2;

    private static LocalTime currentDeliveryTime = DELIVERY_TIME_ONE;
    private static LocalDate currentDeliveryDate = LocalDate.now().plusDays(1);

    private static Stack<String> cancelledOrderDates = new Stack<>();
    private static Stack<Integer> cancelledOrderAssociatedDeliveryPersons = new Stack<>();

    private static boolean deliveryPersonToggle = false;

    public static int assignDeliveryPerson() {

        if (!cancelledOrderAssociatedDeliveryPersons.empty())
            return cancelledOrderAssociatedDeliveryPersons.pop();

        int assignedDeliveryPerson;

        if (deliveryPersonToggle == false)
            assignedDeliveryPerson = 101;
        else
            assignedDeliveryPerson = 102;

        deliveryPersonToggle = !deliveryPersonToggle;

        return assignedDeliveryPerson;
    }

    public static void cancelDelivery(String dateAndTime, Integer deliveryPerson) {
        cancelledOrderDates.add(dateAndTime);
        cancelledOrderAssociatedDeliveryPersons.add(deliveryPerson);

    }

    public static String nextDeliveryTime() {

        if (!cancelledOrderDates.empty())
            return cancelledOrderDates.pop();

        if (currentDeliveryTime == DELIVERY_TIME_ONE && timeOneSlots == 0)
            currentDeliveryTime = DELIVERY_TIME_TWO;
        else if (currentDeliveryTime == DELIVERY_TIME_ONE && timeOneSlots > 0)
            --timeOneSlots;

        if (currentDeliveryTime == DELIVERY_TIME_TWO && timeTwoSlots == 0)
            currentDeliveryTime = DELIVERY_TIME_THREE;
        else if (currentDeliveryTime == DELIVERY_TIME_TWO && timeTwoSlots > 0)
            timeTwoSlots--;

        if (currentDeliveryTime == DELIVERY_TIME_THREE && timeThreeSlots == 0)
            currentDeliveryTime = DELIVERY_TIME_FOUR;
        else if (currentDeliveryTime == DELIVERY_TIME_THREE && timeThreeSlots > 0)
            timeThreeSlots--;

        if (currentDeliveryTime == DELIVERY_TIME_FOUR && timeFourSlots == 0) {
            currentDeliveryTime = DELIVERY_TIME_ONE;
            currentDeliveryDate = incrementDays(currentDeliveryDate);
            timeOneSlots = 1;
            timeTwoSlots = timeThreeSlots = timeFourSlots = 2;
        }
        else if (currentDeliveryTime == DELIVERY_TIME_FOUR && timeFourSlots > 0)
            timeFourSlots--;

        return currentDeliveryDate + " " + currentDeliveryTime;
    }

    public static LocalDate incrementDays(LocalDate date) {
        return date.plusDays(1);
    }
}