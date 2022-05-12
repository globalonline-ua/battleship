package battleship;

import java.io.IOException;
import java.util.*;

public class Main {

    static int[][] usr1Field;
    static int[][] usr2Field;
    static Map<Ships, Integer> usr1Ships;
    static Map<Ships, Integer> usr2Ships;
    static Map<Integer, Character> marks;

    static {
        usr1Ships = new HashMap<>();
        usr2Ships = new HashMap<>();
        usr1Field = new int[10][10];
        Arrays.stream(usr1Field).forEach(a -> Arrays.fill(a, 0));
        usr2Field = new int[10][10];
        Arrays.stream(usr2Field).forEach(a -> Arrays.fill(a, 0));
        marks = new HashMap<>();
        marks.put(0, '~'); // unknown
        marks.put(1, 'M'); // miss
        marks.put(2, 'X'); // hit
        marks.put(5, 'O'); // own ships
    }

    public static void main(String[] args) {

        System.out.println("Player 1, place your ships on the game field\n");
        printField(1);
        fillUsrShips(1);
        fogOfWar(1, 1);

        promptEnterKey();

        System.out.println("Player 2, place your ships on the game field\n");
        printField(2);
        fillUsrShips(2);
        fogOfWar(2, 1);

        promptEnterKey();

        // game
        game();

    }

    private static void promptEnterKey() {
        System.out.println("Press Enter and pass the move to another player");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void game() {

        boolean gameOn = true;
        int user = 1;
        int opponent = 2;
        while (gameOn) {

            fogOfWar(user, 2);
            printField(opponent);
            System.out.println("---------------------");
            printField(user);

            int shot = makeShot(opponent);

            if (shot == 0) {
                System.out.println("You missed. Try again:\n");
            }
            if (shot == 1) {
                System.out.println("You hit a ship! Try again:\n");
            }
            if (shot == 2) {
                System.out.println("You sank a ship! Specify a new target:\n");
            }
            if (shot == 3) {
                System.out.println("You sank the last ship. You won. Congratulations!");
                gameOn = false;
            }

            fogOfWar(user, 1);
            user = user == 1 ? 2 : 1;
            opponent = opponent == 1 ? 2 : 1;

            promptEnterKey();

        }

        System.out.printf("Player %d won. Congratulations!", user);

    }

    public static boolean checkWin(int user) {
        Map<Ships, Integer> tmpMap = (user == 1 ? usr1Ships : usr2Ships);
        int cells = 0;
        for (var ship : tmpMap.entrySet()) {
            cells += ship.getValue();
        }
        return cells == 0;
    }

    private static void printField(int user) {

        System.out.println("  1 2 3 4 5 6 7 8 9 10");
        char ch = 'A';
        int[][] prntField = (user == 1 ? usr1Field : usr2Field);
        for (int[] usrF : prntField) {
            System.out.print(ch++);
            for (int cell : usrF) {
                System.out.print(" " + marks.get(cell % 10));
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    private static int makeShot(int user) {
        Map<Ships, Integer> tmpMap = (user == 1 ? usr1Ships : usr2Ships);
        int[][] tmpUsrArr = (user == 1 ? usr1Field : usr2Field);
        System.out.printf("Player %d, it's your turn:\n", user);
        int result = 0;
        Ships tmpShip = null;
        boolean set = false;
        while (!set) {
            Scanner scanner = new Scanner(System.in);
            String inputStr = scanner.nextLine();
            int pointFromY = letterToInt(inputStr.split(" ")[0].charAt(0));
            int pointFromX = Integer.parseInt(inputStr.split(" ")[0].substring(1)) - 1;

            try {

                if (tmpUsrArr[pointFromY][pointFromX] > 9 && tmpUsrArr[pointFromY][pointFromX] % 10 != 2) {
                    result = 1;
                    for (Ships d : Ships.values()) {
                        if (d.code == tmpUsrArr[pointFromY][pointFromX]) {
                            tmpShip = d;
                        }
                    }
                    tmpUsrArr[pointFromY][pointFromX] += 2;
                }

                if (tmpUsrArr[pointFromY][pointFromX] == 0) {
                    tmpUsrArr[pointFromY][pointFromX] = 1;
                }


            } catch (IndexOutOfBoundsException e) {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
                continue;
            }

            set = true;
        }

        if (user == 1) {
            usr1Field = tmpUsrArr;
        } else {
            usr2Field = tmpUsrArr;
        }

        if (result == 1) {
            if (tmpMap.get(tmpShip) == 1) {
                tmpMap.replace(tmpShip, tmpMap.get(tmpShip) - 1);
                result = 2;
            }
            if (tmpMap.get(tmpShip) > 1) {
                tmpMap.replace(tmpShip, tmpMap.get(tmpShip) - 1);
            }
        }

        if (checkWin(user)) {
            result = 3;
        }

        return result;
    }

    private static int letterToInt(char ch) {
        return (int) ch - 65;
    }

    private static void fillUsrShips(int user) {

        // get input ship coordinates
        for (Ships ship : EnumSet.allOf(Ships.class)) {

            System.out.printf("Enter the coordinates of %s (%d cells):\n", ship.name, ship.base);
            boolean set = false;
            while (!set) {
                Scanner scanner = new Scanner(System.in);
                String inputStr = scanner.nextLine();
                int pointFromY = letterToInt(inputStr.split(" ")[0].charAt(0));
                int pointFromX = Integer.parseInt(inputStr.split(" ")[0].substring(1)) - 1;
                int pointToY = letterToInt(inputStr.split(" ")[1].charAt(0));
                int pointToX = Integer.parseInt(inputStr.split(" ")[1].substring(1)) - 1;

                //process ship location check
                if (!((pointFromX == pointToX && pointFromY != pointToY) || (pointFromX != pointToX && pointFromY == pointToY))) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    continue;
                }

                // process ship base check
                if ((Math.abs(pointToX - pointFromX) != ship.base - 1) && (Math.abs(pointToY - pointFromY) != ship.base - 1)) {
                    System.out.printf("Error! Wrong length of the %s! Try again:\n", ship.getName());
                    continue;
                }

                // convert to array coordinates
                int c1X = Math.min(pointFromX, pointToX);
                int c1Y = Math.min(pointFromY, pointToY);
                int c2X = Math.max(pointFromX, pointToX);
                int c2Y = Math.max(pointFromY, pointToY);

                // check if close to another ships
                if(!checkShips(user, c1X, c1Y, c2X, c2Y)) {
                    System.out.println("Error! You placed it too close to another one. Try again:");
                    continue;
                }

                // set ship
                setShip(user, ship, c1X, c1Y, c2X, c2Y);
                printField(user);

                set = true;
            }

        }
    }

    private static void setShip(int user, Ships ship, int c1X, int c1Y, int c2X, int c2Y) {
        Map<Ships, Integer> tmpMap = (user == 1 ? usr1Ships : usr2Ships);
        int[][] tmpUsrArr = (user == 1 ? usr1Field : usr2Field);
        int cellsCount = 0;

        for (int i = c1Y; i <= c2Y; i++) {
            for (int j = c1X; j <= c2X; j++) {
                tmpUsrArr[i][j] = ship.getCode() + 5;
                cellsCount++;
            }
        }

        tmpMap.put(ship, cellsCount);

        if (user == 1) {
            usr1Field = tmpUsrArr;
        } else {
            usr2Field = tmpUsrArr;
        }
    }

    private static void fogOfWar(int user, int action) {
        int[][] tmpUsrArr = (user == 1 ? usr1Field : usr2Field);

        for (int i = 0; i < tmpUsrArr.length; i++) {
            for (int j = 0; j < tmpUsrArr[i].length; j++) {
                if (action == 1 && tmpUsrArr[i][j] > 9 && (tmpUsrArr[i][j] % 10) == 5) {
                    tmpUsrArr[i][j] -= 5;
                }
                if (action == 2 && tmpUsrArr[i][j] > 9 && (tmpUsrArr[i][j] % 10) == 0) {
                    tmpUsrArr[i][j] += 5;
                }
            }

        }

        if (user == 1) {
            usr1Field = tmpUsrArr;
        } else {
            usr2Field = tmpUsrArr;
        }
    }

    private static boolean checkShips(int user, int c1X, int c1Y, int c2X, int c2Y) {
        int[][] tmpUsrArr = (user == 1 ? usr1Field : usr2Field);

        for (int i = c1Y; i <= c2Y; i++) {
            for (int j = c1X; j <= c2X; j++) {

                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        try {
                            if (tmpUsrArr[i + k][j + l] > 0) {
                                return false;
                            }
                        } catch (IndexOutOfBoundsException ignored) {
                        }
                    }
                }

            }
        }

        return true;
    }



    enum Ships {
        ACS ("Aircraft Carrier", 5, 50),
        BTS ("Battleship", 4,40),
        SBS ("Submarine", 3, 30),
        CRS ("Cruiser", 3, 20),
        DSS ("Destroyer", 2, 10);

        final String name;
        final int base;
        final int code;

        Ships(String name, int base, int code) {
            this.name = name;
            this.base = base;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public int getBase() {
            return base;
        }

        public int getCode() {
            return code;
        }
    }
}
