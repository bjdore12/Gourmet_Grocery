/*

    Author:     Benjamin J. Dore
    Date:       10/17/2020

    Description:
        The Scheduler handles all scheduling in the systems. The main responsibility of this class is to assign
        a delivery date/time and a delivery person to an order.

*/


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Scheduler {

    // These are the fixed delivery times that are available on a given delivery day.
    final private static LocalTime DELIVERY_TIME_ONE = LocalTime.of(9,00);
    final private static LocalTime DELIVERY_TIME_TWO = LocalTime.of(11,00);
    final private static LocalTime DELIVERY_TIME_THREE = LocalTime.of(14,00);
    final private static LocalTime DELIVERY_TIME_FOUR = LocalTime.of(16, 00);

    // Each delivery time slot has two openings for each delivery person (Alan and Barb).
    private static int timeOneSlots = 2;
    private static int timeTwoSlots = 2;
    private static int timeThreeSlots = 2;
    private static int timeFourSlots = 2;

    // The current delivery time intially should be the 09:00 slot
    private static LocalTime currentDeliveryTime = DELIVERY_TIME_ONE;
    private static LocalDate currentDeliveryDate;

    private static boolean deliveryPersonToggle = false;

    static {
        try {
            // Current delivery date is either the next day or the latest delivery date in the system (if there are open time slots)
            currentDeliveryDate = getLatestDeliveryTime();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    // List holds all dates (and associated delivery persons) that we're cancelled. Used to allocate orders to open delivery slots that
    // were previously cancelled.
    private static List<String> cancelledOrderDates = new LinkedList<>();

    // Assign a delivery person to an order
    public static int assignDeliveryPerson() {
        if (!cancelledOrderDates.isEmpty())     // Check cancelled dates first and allocate these if there are any.
            return Integer.parseInt(cancelledOrderDates.remove(0).split(",")[1]);

        int assignedDeliveryPerson;

        if (deliveryPersonToggle == false)
            assignedDeliveryPerson = 101;
        else
            assignedDeliveryPerson = 102;

        deliveryPersonToggle = !deliveryPersonToggle;

        return assignedDeliveryPerson;
    }

    // Function will collect cancelled delivery dates/time and associated delivery persons and add them to a list.
    public static void cancelDelivery(String dateAndTime, int deliveryPerson) {
        cancelledOrderDates.add(dateAndTime + "," + deliveryPerson);
    }

    // This function runs on startup and is used to get back all the cancelled delivery dates (the LinkedList that holds these loses it's
    // contents on system exit)
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

    // If there are possible openings between the specified date and the earliest delivery date, then this function will
    // make sure that date and time slots are added to the cancelledDeliveryDates list.
    private static void addMissingDeliveryTimesToQueueForPastDates(String date,
                                                                   int timeOneAdds,
                                                                   int timeTwoAdds,
                                                                   int timeThreeAdds,
                                                                   int timeFourAdds)  {

        // Add missing 09:00 slot
        while(timeOneAdds != 0) {
            if (timeOneAdds == 2)
                deliveryPersonToggle = false;
            else if (timeOneAdds == 1) {
                deliveryPersonToggle = true;
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 09:00,"+101);
            else
                cancelledOrderDates.add(date + " 09:00,"+102);

            timeOneAdds--;
        }

        // Add missing 11:00 slot
        while(timeTwoAdds != 0) {
            if (timeTwoAdds == 2)
                deliveryPersonToggle = false;
            else if (timeTwoAdds == 1) {
                deliveryPersonToggle = true;
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 11:00,"+101);
            else
                cancelledOrderDates.add(date + " 11:00,"+102);

            timeTwoAdds--;
        }

        // Add missing 14:00 slot
        while(timeThreeAdds != 0) {
            if (timeThreeAdds == 2)
                deliveryPersonToggle = false;
            else if (timeThreeAdds == 1) {
                deliveryPersonToggle = true;
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 14:00,"+101);
            else
                cancelledOrderDates.add(date + " 14:00,"+102);

            timeThreeAdds--;
        }

        // Add missing 16:00 slot
        while(timeFourAdds != 0) {
            if (timeFourAdds == 2)
                deliveryPersonToggle = false;
            else if (timeFourAdds == 1) {
                deliveryPersonToggle = true;
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 16:00,"+101);
            else
                cancelledOrderDates.add(date + " 16:00,"+102);

            timeFourAdds--;
        }
    }

    // This function works on delivery dates that exist in the system, but have gaps in the delivery times.
    // Collects all those gaps and adds them to the cancelledDeliveryDates list.
    private static void addMissingDeliveryTimesToQueue(String date,
                                                       int timeOneAdds,
                                                       int timeTwoAdds,
                                                       int timeThreeAdds,
                                                       int timeFourAdds) {

        // Add missing 09:00 slot
        while (timeOneAdds != 0) {
            if (timeOneAdds == 2)           // Both times slots are empty, Barb should take the first
                deliveryPersonToggle = false;
            else if (timeOneAdds == 1) {
                try {
                    // Figure out who is delivering for the other time slot
                    int deliveryPersonTimeOne = OrderDB.getDeliveryTimesForADateWithDeliveryPerson(date, "09:00").getInt("DeliveryPerson_TUID");

                    // If its Barb, then Alan is next
                    if (deliveryPersonTimeOne == 101)
                        deliveryPersonToggle = true;
                    else
                        deliveryPersonToggle = false;   // Otherwise assign Barb
                } catch (Exception ex) {
                    deliveryPersonToggle = true;
                }
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 09:00," + 101);
            else
                cancelledOrderDates.add(date + " 09:00," + 102);


            timeOneAdds--;
        }

        // Add missing 11:00 slot
        while (timeTwoAdds != 0) {
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
                    deliveryPersonToggle = true;
                }
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 11:00," + 101);
            else
                cancelledOrderDates.add(date + " 11:00," + 102);

            timeTwoAdds--;
        }

        // Add missing 14:00 slot
        while (timeThreeAdds != 0) {
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
                    deliveryPersonToggle = true;
                }
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 14:00," + 101);
            else
                cancelledOrderDates.add(date + " 14:00," + 102);

            timeThreeAdds--;
        }

        // Add missing 16:00 slot
        while (timeFourAdds != 0) {
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
                    deliveryPersonToggle = true;
                }
            }

            if (deliveryPersonToggle == false)
                cancelledOrderDates.add(date + " 16:00," + 101);
            else
                cancelledOrderDates.add(date + " 16:00," + 102);

            timeFourAdds--;
        }

        currentDeliveryDate = incrementDays(currentDeliveryDate);
        deliveryPersonToggle = false;
    }

    // Get the next possible delivery time for an order
    public static String nextDeliveryTime() {

        // Check the cancelledOrderDates first, this needs to be sorted so you are getting the earliest possible date
        if (!cancelledOrderDates.isEmpty()) {
            Collections.sort(cancelledOrderDates, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return object1.compareTo(object2);
                }
            });
            return cancelledOrderDates.get(0).split(",")[0];    // Give me the earliest possible date that was previously cancelled before.
        }

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

    // Get the next day for a date.
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
                timeOneSlots -= (timeOneSlots -= count) < 0 ? 0 : count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("11:00:00")) {
                timeTwoSlots -= (timeTwoSlots -= count) < 0 ? 0 : count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("14:00:00")) {
                timeThreeSlots -= (timeThreeSlots -= count) < 0 ? 0 : count;
                for (int i = 0; i < count; i++) deliveryPersonToggle = !deliveryPersonToggle;
            }

            if (timeSlot.equals("16:00:00")) {
                timeFourSlots -= (timeFourSlots -= count) < 0 ? 0 : count;
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
        } else {
            latest = LocalDate.now().plusDays(1);
        }
        return latest;
    }

    // Look for the earliest delivery date in the Order_Table, if none exists, the earliest date is tomroow.
    public static LocalDate getEarliestDeliveryTime() throws SQLException {
        LocalDate earliestDateInDB;
        if (OrderDB.getLastestDeliveryDate().getString("latestDeliveryDate") != null) {
            String[] earliestDate = OrderDB.getEarliestDeliveryDate().getString("earliestDeliveryDate").split("-");
            earliestDateInDB = LocalDate.of(Integer.parseInt(earliestDate[0]), Integer.parseInt(earliestDate[1]), Integer.parseInt(earliestDate[2]));
        } else {
            return LocalDate.now().plusDays(1);
        }

        LocalDate earliest = LocalDate.now().plusDays(1).compareTo(earliestDateInDB) == -1 ? LocalDate.now().plusDays(1) : earliestDateInDB;
        return earliest;
    }
}