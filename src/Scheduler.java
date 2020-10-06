import java.sql.SQLException;
import java.time.LocalDate;

public class Scheduler {
    // TODO: The scheduler will need to be able to be able to assign delivery personnel and delivery dates to an order.
    private static LocalDate earliestDeliveryDate = LocalDate.now();

    final private static String DELIVERY_TIME_ONE = "09:00";
    final private static String DELIVERY_TIME_TWO = "11:00";
    final private static String DELIVERY_TIME_THREE = "14:00";
    final private static String DELIVERY_TIME_FOUR = "16:00";

    private static boolean deliveryPersonToggle = false;

    public static boolean assignDeliveryPerson() throws SQLException, ClassNotFoundException {

        if (deliveryPersonToggle == false)
            System.out.println(DeliveryPersonDB.getDeliveryPersons(101).getString("Name"));
        else
            System.out.println(DeliveryPersonDB.getDeliveryPersons(102).getString("Name"));

        deliveryPersonToggle = !deliveryPersonToggle;

        return true;
    }

    public static void deliveryPersonRemoved() {
        if (deliveryPersonToggle)
            deliveryPersonToggle = !deliveryPersonToggle;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        assignDeliveryPerson();
        assignDeliveryPerson();
        deliveryPersonRemoved();
        assignDeliveryPerson();
        assignDeliveryPerson();

    }


}