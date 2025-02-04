import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class NQueensSolverVisualizer extends JPanel {
    private int[] queens;
    private int n;
    private static final int CELL_SIZE = 50;
    private static final int DELAY = 390;
    private List<int[]> solutions = new ArrayList<>();
    private int currentSolutionIndex = 0;
    private final Semaphore semaphore = new Semaphore(0);

    public NQueensSolverVisualizer(int n) {
        this.n = n;
        queens = new int[n];
        for (int i = 0; i < n; i++) {
            queens[i] = -1;
        }
        setPreferredSize(new Dimension(n * CELL_SIZE, n * CELL_SIZE));
    }

    public void findAllSolutions() {
        solve(0);
        showAllSolutionsFoundDialog();
    }

    private boolean solve(int row) {
        if (row == n) {
            int[] solution = queens.clone();
            solutions.add(solution);
            showSolutionFoundDialog();
            waitForUser();
            return true;
        }
        boolean found = false;
        for (int col = 0; col < n; col++) {
            if (isSafe(row, col)) {
                queens[row] = col;
                repaintAndSleep();
                if (solve(row + 1)) {
                    found = true;
                }
                queens[row] = -1;
                repaintAndSleep();
            }
        }
        return found;
    }

    private boolean isSafe(int row, int col) {
        for (int i = 0; i < row; i++) {
            if (queens[i] == col || Math.abs(queens[i] - col) == Math.abs(i - row)) {
                return false;
            }
        }
        return true;
    }

    private void repaintAndSleep() {
        repaint();
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void showSolutionFoundDialog() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "SOLUTION FOUND. SOLUTION_COUNT= " + solutions.size());
            semaphore.release();
        });
    }

    private void showAllSolutionsFoundDialog() {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(this, "ALL SOLUTIONS FOUND. SOLUTION_COUNT = " + solutions.size() + "\nDo you want to play again or quit?", "All Solutions Found",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Play Again", "Quit"}, "Quit");
            if (choice == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        });
    }

    private void waitForUser() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void displayNextSolution() {
        if (!solutions.isEmpty()) {
            currentSolutionIndex = (currentSolutionIndex + 1) % solutions.size();
            queens = solutions.get(currentSolutionIndex).clone();
            repaint();
        }
    }

    private void printAllSolutions() {
        for (int[] solution : solutions) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (solution[i] == j) {
                        System.out.print("Q ");
                    } else {
                        System.out.print(". ");
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
        System.out.println("Total solutions: " + solutions.size());
    }

    private void restartGame() {
        String input = JOptionPane.showInputDialog("Welcom, Suraj Gupta Lets Play a Chess Game. Enter the size of the board (n):");
        int newN;
        try {
            newN = Integer.parseInt(input);
            if (newN <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a positive integer.");
            return;
        }
        n = newN;
        queens = new int[n];
        solutions.clear();
        currentSolutionIndex = 0;
        for (int i = 0; i < n; i++) {
            queens[i] = -1;
        }
        setPreferredSize(new Dimension(n * CELL_SIZE, n * CELL_SIZE));
        revalidate();
        repaint();
        new Thread(() -> {
            findAllSolutions();
            printAllSolutions();
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if ((row + col) % 2 == 0) {
                    g2d.setColor(Color.WHITE);
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (queens[row] == col) {
                    g.setColor(Color.RED);
                    g.fillOval(col * CELL_SIZE + 10, row * CELL_SIZE + 10, CELL_SIZE - 20, CELL_SIZE - 20);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String input = JOptionPane.showInputDialog("Welcome, Suraj Gupta Lets Fix a Queen. Enter the size of the board (n):");
            int n;
            try {
                n = Integer.parseInt(input);
                if (n <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a positive integer.");
                return;
            }

            NQueensSolverVisualizer solverVisualizer = new NQueensSolverVisualizer(n);
            JFrame frame = new JFrame("N-Queens Solver");
            frame.add(solverVisualizer);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Thread(() -> {
                solverVisualizer.findAllSolutions();
                solverVisualizer.printAllSolutions();
            }).start();

            JButton nextButton = new JButton("Next Solution");
            nextButton.addActionListener(e -> {
                solverVisualizer.displayNextSolution();
            });
            frame.add(nextButton, BorderLayout.SOUTH);
        });
    }
}