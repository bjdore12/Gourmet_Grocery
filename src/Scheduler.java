import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
    final private static LocalTime DELIVERY_TIME_ONE = LocalTime.of(9,00);
    final private static LocalTime DELIVERY_TIME_TWO = LocalTime.of(11,00);
    final private static LocalTime DELIVERY_TIME_THREE = LocalTime.of(14,00);
    final private static LocalTime DELIVERY_TIME_FOUR = LocalTime.of(16, 00);

    // TODO: These timeslots reset once program execution ends, this could lead to an issue where delivery time slots are overassigned.
    private static int timeOneSlots = 2;
    private static int timeTwoSlots = 2;
    private static int timeThreeSlots = 2;
    private static int timeFourSlots = 2;

    private static LocalTime currentDeliveryTime = DELIVERY_TIME_ONE;
    private static LocalDate currentDeliveryDate = LocalDate.now().plusDays(1);     // TODO: currentDeliveryDate needs to always be the recent date in Order_Table

    private static Queue<String> cancelledOrderDates = new LinkedList<>();
    private static Queue<Integer> cancelledOrderAssociatedDeliveryPersons = new LinkedList<>();

    private static boolean deliveryPersonToggle = false;

    public static int assignDeliveryPerson() {
        if (!cancelledOrderAssociatedDeliveryPersons.isEmpty())
            return cancelledOrderAssociatedDeliveryPersons.poll();

        int assignedDeliveryPerson;

        if (deliveryPersonToggle == false)
            assignedDeliveryPerson = 101;
        else
            assignedDeliveryPerson = 102;

        deliveryPersonToggle = !deliveryPersonToggle;

        return assignedDeliveryPerson;
    }

    public static void cancelDelivery(String dateAndTime, Integer deliveryPerson) {
        cancelledOrderDates.offer(dateAndTime);
        cancelledOrderAssociatedDeliveryPersons.offer(deliveryPerson);
    }

    public static void loadExistingOrderDatesFromDatabase() {
        // TODO: We need a way to get back all of the cancelled order dates that may exist after program execution ends
    }

    public static String nextDeliveryTime() {

        if (!cancelledOrderDates.isEmpty())
            return cancelledOrderDates.poll();

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