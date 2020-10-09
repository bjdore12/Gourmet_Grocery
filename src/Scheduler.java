import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
    final private static LocalTime DELIVERY_TIME_ONE = LocalTime.of(9,00);
    final private static LocalTime DELIVERY_TIME_TWO = LocalTime.of(11,00);
    final private static LocalTime DELIVERY_TIME_THREE = LocalTime.of(14,00);
    final private static LocalTime DELIVERY_TIME_FOUR = LocalTime.of(16, 00);

    private static int timeOneSlots = 2;
    private static int timeTwoSlots = 2;
    private static int timeThreeSlots = 2;
    private static int timeFourSlots = 2;

    private static LocalTime currentDeliveryTime = DELIVERY_TIME_ONE;
    private static LocalDate currentDeliveryDate;

    private static boolean deliveryPersonToggle = false;

    static {
        try {
            currentDeliveryDate = getLatestDeliveryTime();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Queue<String> cancelledOrderDates = new LinkedList<>();
    private static Queue<Integer> cancelledOrderAssociatedDeliveryPersons = new LinkedList<>();

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

    public static void loadCancelledOrderDatesFromDatabaseOnStartup() {
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

    // For the latest delivery date, check each time slot and see how many have already been filled.
    // Subtract the used slots from avaliable and flip the delivery person as such.
    public static boolean decrementTimeSlotsOnStartup() throws SQLException {
        ResultSet res = OrderDB.getLastestDeliveryDateTimeSlots();

        while (res.next()) {
            String timeSlot = res.getString("deliveryTime");
            int count = res.getInt("count");

            if (timeSlot.equals("09:00:00")) {
                timeOneSlots -= count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("11:00:00")) {
                timeTwoSlots -= count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("14:00:00")) {
                timeThreeSlots -= count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("16:00:00")) {
                timeFourSlots -= count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }
        }
        return deliveryPersonToggle;
    }

    // Look for the latest delivery date in the Order_Table, if none exists, the latest delivery date is tomorrow.
    // If the table includes the latest delivery date, then check to see if there are any open time slots on that day.
    public static LocalDate getLatestDeliveryTime() throws SQLException {
        LocalDate latest;
        if (OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate") != null) {
            String[] latestDate = OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate").split("-");
            latest = LocalDate.of(Integer.parseInt(latestDate[0]), Integer.parseInt(latestDate[1]), Integer.parseInt(latestDate[2]));
            decrementTimeSlotsOnStartup();
        } else {
            latest = LocalDate.now().plusDays(1);
        }
        return latest;
    }

    public static void main(String[] args) {

            System.out.println(currentDeliveryTime);
            System.out.println("Time one: " + timeOneSlots);
            System.out.println("Time two: " + timeTwoSlots);
            System.out.println("Time three: " + timeThreeSlots);
            System.out.println("Time four: " + timeFourSlots);
    }
}