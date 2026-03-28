package JavaFirst;

import java.io.*;
import java.util.*;

public class RailwayReservationSystem {

    // ---------- Models ----------
    static class Train {
        int trainNo; String from; String to; String departure;
        int fare; int totalSeats; int bookedSeats;
        Train(int trainNo, String from, String to, String departure, int fare, int seats) {
            this.trainNo = trainNo; this.from = from; this.to = to;
            this.departure = departure; this.fare = fare;
            this.totalSeats = seats; this.bookedSeats = 0;
        }
    }

    static class Passenger {
        int pnr; String name; int trainNo; String classType; int seatNo; Passenger next;
        Passenger(int pnr, String name, int trainNo, String classType, int seatNo) {
            this.pnr = pnr; this.name = name; this.trainNo = trainNo;
            this.classType = classType; this.seatNo = seatNo;
        }
    }

    // ---------- Stack for recent searches ----------
    static class IntStack {
        private final Deque<Integer> stack = new ArrayDeque<>();
        void push(int v) { stack.push(v); }
        void display() {
            if (stack.isEmpty()) { System.out.println("No recent searches."); return; }
            System.out.println("Recently searched trains (latest first):");
            for (int v : stack) System.out.println(v);
        }
    }

    // ---------- Queue for waiting list ----------
    static class PassengerQueue {
        private final Queue<Passenger> q = new ArrayDeque<>();
        void enqueue(Passenger p) { q.offer(p); }
        Passenger dequeue() { return q.poll(); }
        boolean isEmpty() { return q.isEmpty(); }
        boolean removeByPNR(int pnr) {
            Queue<Passenger> tmp = new ArrayDeque<>();
            boolean removed = false;
            while (!q.isEmpty()) {
                Passenger p = q.poll();
                if (p.pnr == pnr) { removed = true; continue; }
                tmp.offer(p);
            }
            q.addAll(tmp);
            return removed;
        }
        void display() {
            if (q.isEmpty()) { System.out.println("Waiting list empty."); return; }
            System.out.println("Waiting List:");
            for (Passenger p : q) {
                System.out.println("PNR: " + p.pnr + " Name: " + p.name +
                        " Train: " + p.trainNo + " Class: " + p.classType);
            }
        }
    }

    // ---------- Core fields ----------
    private final Train[] trains = new Train[100];
    private int trainCount = 0;

    private Passenger head = null;
    private int nextPNR = 1;

    private final PassengerQueue waitingList = new PassengerQueue();
    private final IntStack recentSearches = new IntStack();
    private final Scanner sc = new Scanner(System.in);

    public RailwayReservationSystem() {
        loadTrainsFromFile();
        quickSortTrains(0, trainCount - 1);
    }

    // ---------- File handling (tries relative, then absolute) ----------
    private void loadTrainsFromFile() {
        String[] paths = { "train.txt", "D:/javadsa/train.txt" }; // adjust or add more paths if needed
        for (String pth : paths) {
            File file = new File(pth);
            if (!file.exists()) continue;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] p = line.split("\\|");
                    int trainNo = Integer.parseInt(p[0].trim());
                    String from = p[1].trim();
                    String to = p[2].trim();
                    String dep = p[3].trim();
                    int fare = Integer.parseInt(p[4].trim());
                    int seats = (p.length > 5) ? Integer.parseInt(p[5].trim()) : 10;
                    trains[trainCount++] = new Train(trainNo, from, to, dep, fare, seats);
                }
                System.out.println("Loaded trains from: " + file.getAbsolutePath());
                return;
            } catch (Exception e) {
                System.out.println("Error reading " + file.getAbsolutePath() + ": " + e.getMessage());
                return;
            }
        }
        System.out.println("train.txt not found. Tried: " + Arrays.toString(paths));
    }

    // ---------- Quick sort trains by trainNo ----------
    private void quickSortTrains(int lo, int hi) {
        if (lo < hi) {
            int p = partitionTrains(lo, hi);
            quickSortTrains(lo, p - 1);
            quickSortTrains(p + 1, hi);
        }
    }
    private int partitionTrains(int lo, int hi) {
        int pivot = trains[hi].trainNo;
        int i = lo - 1;
        for (int j = lo; j < hi; j++) if (trains[j].trainNo <= pivot) { i++; swapTrains(i, j); }
        swapTrains(i + 1, hi); return i + 1;
    }
    private void swapTrains(int a, int b) { Train t = trains[a]; trains[a] = trains[b]; trains[b] = t; }

    // ---------- Quick sort passengers by PNR ----------
    private void quickSortPassengers(Passenger[] arr, int lo, int hi) {
        if (lo < hi) {
            int p = partitionPassengers(arr, lo, hi);
            quickSortPassengers(arr, lo, p - 1);
            quickSortPassengers(arr, p + 1, hi);
        }
    }
    private int partitionPassengers(Passenger[] arr, int lo, int hi) {
        int pivot = arr[hi].pnr;
        int i = lo - 1;
        for (int j = lo; j < hi; j++) if (arr[j].pnr <= pivot) { i++; swapPassengers(arr, i, j); }
        swapPassengers(arr, i + 1, hi); return i + 1;
    }
    private void swapPassengers(Passenger[] arr, int a, int b) { Passenger t = arr[a]; arr[a] = arr[b]; arr[b] = t; }
    private void rebuildPassengerList(Passenger[] arr) {
        head = null; Passenger tail = null;
        for (Passenger p : arr) {
            p.next = null;
            if (head == null) { head = tail = p; }
            else { tail.next = p; tail = p; }
        }
    }

    // ---------- Searching ----------
    private int binarySearchTrain(int trainNo) {
        int l = 0, r = trainCount - 1;
        while (l <= r) {
            int mid = (l + r) / 2;
            if (trains[mid].trainNo == trainNo) return mid;
            if (trains[mid].trainNo < trainNo) l = mid + 1; else r = mid - 1;
        }
        return -1;
    }
    private Passenger findPassengerByPNR(int pnr) {
        for (Passenger p = head; p != null; p = p.next) if (p.pnr == pnr) return p;
        return null;
    }

    // ---- Booking: class asked once, multiple tickets, total fare shown ----
    private void bookTicket() {
        System.out.print("Enter Train No: ");
        int trainNo = readInt();
        int idx = binarySearchTrain(trainNo);
        if (idx == -1) { System.out.println("Train not found."); return; }
        Train t = trains[idx];

        System.out.print("Enter number of tickets: ");
        int count = Math.max(1, readInt());

        System.out.print("Enter Class for all tickets (AC/Sleeper): ");
        String cls = sc.nextLine().trim();

        int seatsLeft = t.totalSeats - t.bookedSeats;
        int willConfirm = Math.min(count, seatsLeft);

        List<Passenger> booked = new ArrayList<>();
        List<Passenger> waited = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            System.out.print("Enter Name for ticket " + i + ": ");
            String name = sc.nextLine().trim();
            Passenger p = new Passenger(nextPNR++, name, trainNo, cls, 0);

            if (willConfirm > 0) {
                p.seatNo = ++t.bookedSeats;
                appendPassenger(p);
                booked.add(p);
                willConfirm--;
            } else {
                waitingList.enqueue(p);
                waited.add(p);
            }
        }

        if (!booked.isEmpty()) {
            int totalFare = booked.size() * t.fare;
            System.out.println("Confirmed:");
            for (Passenger p : booked) {
                System.out.println("PNR: " + p.pnr + " Seat: " + p.seatNo + " Fare: " + t.fare);
            }
            System.out.println("Total fare (confirmed): " + totalFare);
        }
        if (!waited.isEmpty()) {
            System.out.println("Added to waiting list:");
            for (Passenger p : waited) {
                System.out.println("PNR: " + p.pnr + " (waiting)");
            }
        }
    }

    private void appendPassenger(Passenger p) {
        if (head == null) head = p;
        else { Passenger temp = head; while (temp.next != null) temp = temp.next; temp.next = p; }
    }

    // ---------- Cancellation ----------
    private void cancelTicket() {
        System.out.print("Enter PNR to cancel: ");
        int pnr = readInt();

        Passenger prev = null, curr = head;
        while (curr != null && curr.pnr != pnr) { prev = curr; curr = curr.next; }

        if (curr == null) {
            if (waitingList.removeByPNR(pnr)) {
                System.out.println("Cancelled from waiting list.");
            } else {
                System.out.println("PNR not found.");
            }
            return;
        }

        if (prev == null) head = curr.next; else prev.next = curr.next;

        Train t = trains[binarySearchTrain(curr.trainNo)];
        if (curr.seatNo > 0 && t.bookedSeats > 0) t.bookedSeats--;

        System.out.println("Cancellation successful.");

        if (!waitingList.isEmpty()) {
            Passenger w = waitingList.dequeue();
            w.seatNo = ++t.bookedSeats;
            appendPassenger(w);
            System.out.println("Waiting PNR " + w.pnr + " moved to confirmed with seat " + w.seatNo + ".");
        }
    }

    // ---------- Display ----------
    private void displayTrains() {
        if (trainCount == 0) { System.out.println("No trains loaded."); return; }
        System.out.println("Trains:");
        for (Train t : trains) {
            if (t == null) break;
            System.out.println("No: " + t.trainNo + " " + t.from + " -> " + t.to +
                    " At: " + t.departure + " Fare: " + t.fare +
                    " Seats: " + t.bookedSeats + "/" + t.totalSeats);
        }
    }

    private void displayPassengers() {
        if (head == null) { System.out.println("No confirmed passengers."); return; }
        List<Passenger> list = new ArrayList<>();
        for (Passenger p = head; p != null; p = p.next) list.add(p);
        Passenger[] arr = list.toArray(new Passenger[0]);
        quickSortPassengers(arr, 0, arr.length - 1);
        rebuildPassengerList(arr);
        System.out.println("Confirmed Passengers (by PNR):");
        for (Passenger p : headIterable()) {
            System.out.println("PNR: " + p.pnr + " Name: " + p.name +
                    " Train: " + p.trainNo + " Seat: " + p.seatNo +
                    " Class: " + p.classType);
        }
    }

    private Iterable<Passenger> headIterable() {
        return () -> new Iterator<Passenger>() {
            Passenger cur = head;
            public boolean hasNext() { return cur != null; }
            public Passenger next() { Passenger r = cur; cur = cur.next; return r; }
        };
    }

    // ---------- Train search with stack push ----------
    private void searchTrain() {
        System.out.print("Enter Train No: ");
        int no = readInt();
        recentSearches.push(no);
        int idx = binarySearchTrain(no);
        if (idx == -1) { System.out.println("Not found."); return; }
        Train t = trains[idx];
        System.out.println("Train " + t.trainNo + " " + t.from + " -> " + t.to +
                " At " + t.departure + " Fare " + t.fare +
                " Seats " + t.bookedSeats + "/" + t.totalSeats);
    }

    private void searchPassenger() {
        System.out.print("Enter PNR: ");
        int pnr = readInt();
        Passenger p = findPassengerByPNR(pnr);
        if (p == null) { System.out.println("Not found."); return; }
        System.out.println("PNR: " + p.pnr + " Name: " + p.name +
                " Train: " + p.trainNo + " Seat: " + p.seatNo +
                " Class: " + p.classType);
    }

    // ---------- Helpers ----------
    private int readInt() {
        while (true) {
            String line = sc.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.print("Invalid number, retry: "); }
        }
    }

    // ---------- Menu ----------
    private void menu() {
        int choice;
        do {
            System.out.println("\n--- Railway Reservation System ---");
            System.out.println("1. Display Trains");
            System.out.println("2. Search Train by Number");
            System.out.println("3. Book Ticket");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Display Passengers");
            System.out.println("6. Search Passenger by PNR");
            System.out.println("7. Show Waiting List");
            System.out.println("8. Show Recently Searched Trains (Stack)");
            System.out.println("9. Exit");
            System.out.print("Enter choice: ");
            choice = readInt();

            switch (choice) {
                case 1: displayTrains(); break;
                case 2: searchTrain(); break;
                case 3: bookTicket(); break;
                case 4: cancelTicket(); break;
                case 5: displayPassengers(); break;
                case 6: searchPassenger(); break;
                case 7: waitingList.display(); break;
                case 8: recentSearches.display(); break;
                case 9: System.out.println("Goodbye."); break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 9);
    }

    public static void main(String[] args) {
        new RailwayReservationSystem().menu();
    }
}
