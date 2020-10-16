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

    public static void loadCancelledOrderDatesFromDatabaseOnStartup() throws SQLException {
        ResultSet res = OrderDB.getAllDeliveryDates();

        LocalDate expectedDate = getEarliestDeliveryTime();

        if (OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate") != null) {

            String[] earliestDBDate = OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate").split("-");
            LocalDate earliestDBDateConvert = LocalDate.of(Integer.parseInt(earliestDBDate[0]), Integer.parseInt(earliestDBDate[1]), Integer.parseInt(earliestDBDate[2]));

            // If the expectedDate is less than the earliest date in the orders table.
            while (expectedDate.compareTo(earliestDBDateConvert) == -1) {
                addMissingDeliveryTimesToQueueForPastDates(expectedDate.toString(), 2, 2, 2, 2);
                expectedDate = incrementDays(expectedDate);
            }
        }

        // If the expectedDate is within the dates in the orders table
        while(res.next()) {

            String date = res.getString("deliveryDate");
            if (expectedDate.toString().equals(date)) {
                ResultSet timeOne = OrderDB.getDeliveryTimesForADate(date, "09:00:00");
                ResultSet timeTwo = OrderDB.getDeliveryTimesForADate(date, "11:00:00");
                ResultSet timeThree = OrderDB.getDeliveryTimesForADate(date, "14:00:00");
                ResultSet timeFour = OrderDB.getDeliveryTimesForADate(date, "16:00:00");

                int countOfTimeOne = Integer.parseInt(timeOne.getString("Time_Count"));
                int countOfTimeTwo = Integer.parseInt(timeTwo.getString("Time_Count"));
                int countOfTimeThree = Integer.parseInt(timeThree.getString("Time_Count"));
                int countOfTimeFour = Integer.parseInt(timeFour.getString("Time_Count"));

                int timeOneAdds = Math.abs(countOfTimeOne - 2);
                int timeTwoAdds = Math.abs(countOfTimeTwo - 2);
                int timeThreeAdds = Math.abs(countOfTimeThree - 2);
                int timeFourAdds = Math.abs(countOfTimeFour - 2);

                addMissingDeliveryTimesToQueue(date, timeOneAdds, timeTwoAdds, timeThreeAdds, timeFourAdds);
            } else {
                addMissingDeliveryTimesToQueue(expectedDate.toString(), 2, 2, 2, 2);
            }

            expectedDate = incrementDays(expectedDate);
        }
    }

    private static void addMissingDeliveryTimesToQueueForPastDates(String date, int timeOneAdds, int timeTwoAdds, int timeThreeAdds, int timeFourAdds) throws SQLException {

        // Add missing 09:00 slot
        while(timeOneAdds != 0) {
            if (timeOneAdds == 2)
                deliveryPersonToggle = false;
            else if (timeOneAdds == 1) {
                deliveryPersonToggle = true;
            }

            cancelledOrderDates.offer(date + " 09:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeOneAdds--;
        }

        // Add missing 11:00 slot
        while(timeTwoAdds != 0) {
            if (timeTwoAdds == 2)
                deliveryPersonToggle = false;
            else if (timeTwoAdds == 1) {
                deliveryPersonToggle = true;
            }

            cancelledOrderDates.offer(date + " 11:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeTwoAdds--;
        }

        // Add missing 14:00 slot
        while(timeThreeAdds != 0) {
            if (timeThreeAdds == 2)
                deliveryPersonToggle = false;
            else if (timeThreeAdds == 1) {
                deliveryPersonToggle = true;
            }

            cancelledOrderDates.offer(date + " 14:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeThreeAdds--;
        }

        // Add missing 16:00 slot
        while(timeFourAdds != 0) {
            if (timeFourAdds == 2)
                deliveryPersonToggle = false;
            else if (timeFourAdds == 1) {
                deliveryPersonToggle = true;
            }

            cancelledOrderDates.offer(date + " 16:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeFourAdds--;
        }
    }

    private static void addMissingDeliveryTimesToQueue(String date, int timeOneAdds, int timeTwoAdds, int timeThreeAdds, int timeFourAdds) throws SQLException {

        // Add missing 09:00 slot
        while(timeOneAdds != 0) {
            if (timeOneAdds == 2)
                deliveryPersonToggle = false;
            else if (timeOneAdds == 1) {
                try {
                    int deliveryPersonTimeOne = OrderDB.getDeliveryTimesForADateWithDeliveryPerson(date, "09:00").getInt("DeliveryPerson_TUID");

                    if (deliveryPersonTimeOne == 101)
                        deliveryPersonToggle = true;
                    else
                        deliveryPersonToggle = false;
                } catch (Exception ex) {
                return;
                }
            }

            cancelledOrderDates.offer(date + " 09:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeOneAdds--;
        }

        // Add missing 11:00 slot
        while(timeTwoAdds != 0) {
            if (timeTwoAdds == 2)
                deliveryPersonToggle = false;
            else if (timeTwoAdds == 1) {
                try {
                    int deliveryPersonTimeTwo = OrderDB.getDeliveryTimesForADateWithDeliveryPerson(date, "11:00").getInt("DeliveryPerson_TUID");
                    if (deliveryPersonTimeTwo == 101)
                        deliveryPersonToggle = true;
                    else
                        deliveryPersonToggle = false;
                } catch (Exception ex) {
                    return;
                }
            }


            cancelledOrderDates.offer(date + " 11:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeTwoAdds--;
        }

        // Add missing 14:00 slot
        while(timeThreeAdds != 0) {
            if (timeThreeAdds == 2)
                deliveryPersonToggle = false;
            else if (timeThreeAdds == 1) {
                try {
                    int deliveryPersonTimeThree = OrderDB.getDeliveryTimesForADateWithDeliveryPerson(date, "14:00").getInt("DeliveryPerson_TUID");
                    if (deliveryPersonTimeThree == 101)
                        deliveryPersonToggle = true;
                    else
                        deliveryPersonToggle = false;
                } catch (Exception ex) {
                    return;
                }
            }


            cancelledOrderDates.offer(date + " 14:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeThreeAdds--;
        }

        // Add missing 16:00 slot
        while(timeFourAdds != 0) {
            if (timeFourAdds == 2)
                deliveryPersonToggle = false;
            else if (timeFourAdds == 1) {
                try {
                    int deliveryPersonTimeFour = OrderDB.getDeliveryTimesForADateWithDeliveryPerson(date, "16:00").getInt("DeliveryPerson_TUID");
                    if (deliveryPersonTimeFour == 101)
                        deliveryPersonToggle = true;
                    else
                        deliveryPersonToggle = false;
                } catch (Exception ex) {
                    return;
                }
            }


            cancelledOrderDates.offer(date + " 16:00");

            if (deliveryPersonToggle == false)
                cancelledOrderAssociatedDeliveryPersons.offer(101);
            else
                cancelledOrderAssociatedDeliveryPersons.offer(102);

            timeFourAdds--;
        }
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

    public static LocalDate getEarliestDeliveryTime() throws SQLException {
        LocalDate earliestDateInDB;
        if (OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate") != null) {
            String[] earliestDate = OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate").split("-");
            earliestDateInDB = LocalDate.of(Integer.parseInt(earliestDate[0]), Integer.parseInt(earliestDate[1]), Integer.parseInt(earliestDate[2]));
            decrementTimeSlotsOnStartup();
        } else {
            return LocalDate.now().plusDays(1);
        }

        LocalDate earliest = LocalDate.now().plusDays(1).compareTo(earliestDateInDB) == -1 ? LocalDate.now().plusDays(1) : earliestDateInDB;
        return earliest;
    }

    public static void main(String[] args) throws SQLException {
        System.out.println(getEarliestDeliveryTime());
    }
}