
import java.io.*;
import java.util.Scanner;

public class RailwayReservationSystem {

    Train[] trains = new Train[20];
    int trainCount = 0;

    Passenger head = null;
    int pnrCounter = 1;

    Scanner sc = new Scanner(System.in);

    public RailwayReservationSystem() {
        loadTrainsFromFile();               // Load trains from file
        quickSortTrains(0, trainCount - 1); // Sort trains by trainNo
    }

    // ================= LOAD TRAINS =================
    private void loadTrainsFromFile() {
        File file = new File("train.txt"); // 
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|");
                int trainNo = Integer.parseInt(parts[0]);
                String from = parts[1];
                String destination = parts[2];
                String classType = parts[3];
                int fare = Integer.parseInt(parts[4]);
                trains[trainCount++] = new Train(trainNo, from, destination, classType, fare);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Train file not found at " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error reading trains: " + e.getMessage());
        }
    }

    // ================= QUICK SORT TRAINS BY TRAIN NO =================
    private void quickSortTrains(int start, int end) {
        if (start < end) {
            int pivotIndex = partitionTrains(start, end);
            quickSortTrains(start, pivotIndex - 1);
            quickSortTrains(pivotIndex + 1, end);
        }
    }

    private int partitionTrains(int start, int end) {
        int pivot = trains[end].trainNo;
        int i = start - 1;
        for (int j = start; j < end; j++) {
            if (trains[j].trainNo < pivot) {
                i++;
                Train temp = trains[i];
                trains[i] = trains[j];
                trains[j] = temp;
            }
        }
        Train temp = trains[i + 1];
        trains[i + 1] = trains[end];
        trains[end] = temp;
        return i + 1;
    }

    // ================= DISPLAY TRAINS =================
    public void displayTrains() {
        if (trainCount == 0) {
            System.out.println("No trains available.");
            return;
        }
        System.out.println("\nAvailable Trains:");
        for (int i = 0; i < trainCount; i++) {
            System.out.println("Train No: " + trains[i].trainNo +
                    " From: " + trains[i].from +
                    " To: " + trains[i].destination +
                    " Class: " + trains[i].classType +
                    " Fare: " + trains[i].fare);
        }
    }

    // ================= BOOK TICKET =================
    public void bookTicket() {
        sc.nextLine(); // clear leftover newline after nextInt
        System.out.print("Enter Passenger Name: ");
        String name = sc.nextLine();

        System.out.print("Enter From: ");
        String from = sc.nextLine();

        System.out.print("Enter Destination: ");
        String destination = sc.nextLine();

        System.out.print("Enter Class (AC/Sleeper): ");
        String classType = sc.nextLine();

        Train foundTrain = null;

        // LINEAR SEARCH
        for (int i = 0; i < trainCount; i++) {
            Train t = trains[i];
            if (t.from.equalsIgnoreCase(from) &&
                t.destination.equalsIgnoreCase(destination) &&
                t.classType.equalsIgnoreCase(classType)) {
                foundTrain = t;
                break;
            }
        }

        if (foundTrain == null) {
            System.out.println("Train Not Available!");
            return;
        }

        Passenger newPassenger = new Passenger(
                pnrCounter++,
                name,
                foundTrain.trainNo,
                classType,
                foundTrain.fare
        );

        // Add passenger to linked list
        if (head == null) head = newPassenger;
        else {
            Passenger temp = head;
            while (temp.next != null) temp = temp.next;
            temp.next = newPassenger;
        }

        System.out.println("Ticket Booked Successfully!");
        System.out.println("Train No: " + foundTrain.trainNo);
        System.out.println("PNR: " + newPassenger.pnr);
        System.out.println("Fare: " + foundTrain.fare);
    }

    // ================= DISPLAY PASSENGERS =================
    public void displayPassengers() {
        if (head == null) {
            System.out.println("No passengers found.");
            return;
        }
        Passenger temp = head;
        while (temp != null) {
            System.out.println("PNR: " + temp.pnr +
                    " Name: " + temp.name +
                    " Train No: " + temp.trainNo +
                    " Class: " + temp.classType +
                    " Fare: " + temp.fare);
            temp = temp.next;
        }
    }

    // ================= MENU =================
    public void menu() {
        int choice;
        do {
            System.out.println("\n1. Display Trains");
            System.out.println("2. Book Ticket");
            System.out.println("3. Display Passengers");
            System.out.println("4. Exit");

            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1: displayTrains(); break;
                case 2: bookTicket(); break;
                case 3: displayPassengers(); break;
                case 4: System.out.println("Exiting..."); break;
                default: System.out.println("Invalid choice!");
            }

        } while (choice != 4);
    }

    public static void main(String[] args) {
        new RailwayReservationSystem().menu();
    }
}