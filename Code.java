import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;

public class Asteroids {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}

class GameFrame extends JFrame {
    CardLayout cards = new CardLayout();
    JPanel root = new JPanel(cards);
    LoginPanel loginPanel;
    MenuPanel menuPanel;
    GamePanel gamePanel;
    UserData userData = new UserData();
    String currentUser = null;
    int chosenMap = 0;
    int chosenShip = 0;

    GameFrame() {
        setTitle("Asteroids - 8-bit Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this);
        menuPanel = new MenuPanel(this);
        gamePanel = new GamePanel(this);

        root.add(loginPanel, "LOGIN");
        root.add(menuPanel, "MENU");
        root.add(gamePanel, "GAME");

        add(root);
        cards.show(root, "LOGIN");
        setVisible(true);
    }

    void showMenu() {
        menuPanel.updateLabels();
        cards.show(root, "MENU");
    }

    void startGame() {
        gamePanel.setup(chosenMap, chosenShip, currentUser);
        cards.show(root, "GAME");
        gamePanel.requestFocusInWindow();  // ensure panel has focus
        gamePanel.start();
    }
}


class UserData {
    private final File userFile = new File("users.txt");
    private final File scoreFile = new File("scores.txt");
    Map<String, String> users = new HashMap<>();
    Map<String, Integer> scores = new HashMap<>();

    UserData() {
        loadUsers();
        loadScores();
    }

    void loadUsers() {
        users.clear();
        if (!userFile.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(":",2);
                if (p.length==2) users.put(p[0], p[1]);
            }
        } catch (IOException ignored) {}
    }

    void loadScores() {
        scores.clear();
        if (!scoreFile.exists()) return;
        try (BufferedReader r = new BufferedReader(new FileReader(scoreFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(":",2);
                if (p.length==2) scores.put(p[0], Integer.parseInt(p[1]));
            }
        } catch (IOException ignored) {}
    }

    boolean register(String user, String pass) {
        if (users.containsKey(user) || user.isEmpty()) return false;
        users.put(user, pass);
        saveUsers();
        return true;
    }

    boolean login(String user, String pass) {
        return users.containsKey(user) && users.get(user).equals(pass);
    }

    int getHighScore(String user) {
        return scores.getOrDefault(user, 0);
    }

    void saveScore(String user, int score) {
        int prev = scores.getOrDefault(user, 0);
        if (score > prev) {
            scores.put(user, score);
            saveScores();
        }
    }

    void saveUsers() {
        try (PrintWriter w = new PrintWriter(new FileWriter(userFile))) {
            for (String u : users.keySet()) w.println(u + ":" + users.get(u));
        } catch (IOException ignored) {}
    }

    void saveScores() {
        try (PrintWriter w = new PrintWriter(new FileWriter(scoreFile))) {
            for (String u : scores.keySet()) w.println(u + ":" + scores.get(u));
        } catch (IOException ignored) {}
    }
}

//login page
class LoginPanel extends JPanel {
    GameFrame parent;
    JTextField userField = new JTextField(12);
    JPasswordField passField = new JPasswordField(12);
    JLabel info = new JLabel(" ");

    LoginPanel(GameFrame p) {
        this.parent = p;
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.gridx=0; c.gridy=0; c.gridwidth=2;
        JLabel title = new JLabel("ASTEROIDS");
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        add(title,c);

        c.gridwidth=1; c.gridy++;
        c.gridx=0; add(new JLabel("User:"),c);
        c.gridx=1; add(userField,c);
        c.gridy++; c.gridx=0; add(new JLabel("Pass:"),c);
        c.gridx=1; add(passField,c);

        JButton loginBtn = new JButton("Login");
        JButton regBtn = new JButton("Register");
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String pword = new String(passField.getPassword());
            if (parent.userData.login(u,pword)) {
                parent.currentUser = u;
                info.setText("Logged in as "+u);
                parent.showMenu();
            } else info.setText("Invalid credentials");
        });
        regBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String pword = new String(passField.getPassword());
            if (parent.userData.register(u,pword)) info.setText("Success! Proceed to login.");
            else info.setText("Error - User Exists or Invalid Input");
        });

        c.gridy++; c.gridx=0; add(loginBtn,c);
        c.gridx=1; add(regBtn,c);

        c.gridy++; c.gridx=0; c.gridwidth=2;
        info.setForeground(Color.GREEN);
        add(info,c);
    }
}


class MenuPanel extends JPanel {
    GameFrame parent;
    JLabel hi = new JLabel();
    JLabel mapLabel = new JLabel("Map: Deep Space");
    JLabel shipLabel = new JLabel("Ship: Fighter");
    JButton mapBtn = new JButton("Change Map");
    JButton shipBtn = new JButton("Change Ship");
    JButton startBtn = new JButton("Start Game");
    JButton logoutBtn = new JButton("Logout");
    JLabel scoreLabel = new JLabel();
    JButton leaderboardBtn = new JButton("Show Top Scores");

    String[] maps = {"Deep Space", "Nebula", "Asteroid Belt"};
    String[] ships = {"Fighter","Interceptor","Bomber"};

    MenuPanel(GameFrame p) {
        parent = p;
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.gridx=0; c.gridy=0;
        hi.setForeground(Color.CYAN);
        hi.setFont(new Font("Monospaced", Font.BOLD, 24));
        add(hi,c);

        c.gridy++;
        add(mapLabel,c);
        c.gridy++;
        add(shipLabel,c);

        c.gridy++; add(startBtn,c);
        c.gridy++; add(mapBtn,c);
        c.gridy++; add(shipBtn,c);
        c.gridy++; add(leaderboardBtn,c);
        c.gridy++; add(logoutBtn,c);
        c.gridy++; scoreLabel.setForeground(Color.GREEN); add(scoreLabel,c);

        // keep 'focus' away from menu buttons to make sure the gameplay has it instead
        startBtn.setFocusable(false);
        mapBtn.setFocusable(false);
        shipBtn.setFocusable(false);
        leaderboardBtn.setFocusable(false);
        logoutBtn.setFocusable(false);

        startBtn.addActionListener(e -> parent.startGame());
        mapBtn.addActionListener(e -> {
            parent.chosenMap = (parent.chosenMap +1) % maps.length;
            mapLabel.setText("Map: "+maps[parent.chosenMap]);
        });
        shipBtn.addActionListener(e -> {
            parent.chosenShip = (parent.chosenShip +1) % ships.length;
            shipLabel.setText("Ship: "+ships[parent.chosenShip]);
        });
        logoutBtn.addActionListener(e -> {
            parent.currentUser = null;
            parent.cards.show(parent.root,"LOGIN");
        });
        
       //leaderboard
        leaderboardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("scores.txt");
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(null, "No scores yet!");
                        return;
                    }
                    
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    ArrayList<String> scoreList = new ArrayList<>();
                    
                    while ((line = reader.readLine()) != null) {
                        scoreList.add(line);
                    }
                    reader.close();
                    
                    for (int i = 0; i < scoreList.size(); i++) {
                        for (int j = 0; j < scoreList.size() - 1; j++) {
                            String[] parts1 = scoreList.get(j).split(":");
                            String[] parts2 = scoreList.get(j + 1).split(":");
                            
                            if (parts1.length == 2 && parts2.length == 2) {
                                try {
                                    int score1 = Integer.parseInt(parts1[1]);
                                    int score2 = Integer.parseInt(parts2[1]);
                                    
                                    if (score1 < score2) {
                                        String temp = scoreList.get(j);
                                        scoreList.set(j, scoreList.get(j + 1));
                                        scoreList.set(j + 1, temp);
                                    }
                                } catch (NumberFormatException ex) {}
                            }
                        }
                    }
                    
                    String message = "TOP SCORES:\n";
                    int count = Math.min(5, scoreList.size());
                    
                    for (int i = 0; i < count; i++) {
                        String[] parts = scoreList.get(i).split(":");
                        if (parts.length == 2) {
                            message += (i + 1) + ". " + parts[0] + ": " + parts[1] + "\n";
                        }
                    }
                    
                    JOptionPane.showMessageDialog(null, message);
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading scores!");
                }
            }
        });
    }

    void updateLabels() {
        hi.setText("Pilot: "+parent.currentUser);
        mapLabel.setText("Map: "+maps[parent.chosenMap]);
        shipLabel.setText("Ship: "+ships[parent.chosenShip]);
        scoreLabel.setText("High Score: "+parent.userData.getHighScore(parent.currentUser));
    }
}


abstract class SpaceObject {
    double x, y;
    int w, h;
    double vx, vy;
    Color color = Color.WHITE;
    boolean alive = true;
    
    SpaceObject(double x, double y, int w, int h){
        this.x=x; this.y=y; this.w=w; this.h=h;
    }
    
    abstract void move();
    abstract void draw(Graphics2D g);
    
    boolean collidesWith(SpaceObject other) {
        return x < other.x + other.w && x + w > other.x &&
               y < other.y + other.h && y + h > other.y;
    }
}

class Asteroid extends SpaceObject {
    int size;
    int[][] shapePoints;
    
    Asteroid(double x, double y, int size, double vx, double vy) {
        super(x, y, size==0?60:size==1?40:20, size==0?60:size==1?40:20);
        this.size = size;
        this.vx = vx * 0.5;
        this.vy = vy * 0.5;
        this.color = new Color(150, 150, 150);
        
        shapePoints = new int[8][2];
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8;
            double radius = w/2 * (0.7 + 0.3 * rnd.nextDouble());
            shapePoints[i][0] = (int)(radius * Math.cos(angle));
            shapePoints[i][1] = (int)(radius * Math.sin(angle));
        }
    }
    
    void move() {
        x += vx;
        y += vy;
        
        if (x < -w) x = 800;
        if (x > 800) x = -w;
        if (y < -h) y = 600;
        if (y > 600) y = -h;
    }
    
    void draw(Graphics2D g) {
        int centerX = (int)x + w/2;
        int centerY = (int)y + h/2;
        
        g.setColor(color);
        for (int i = 0; i < shapePoints.length; i++) {
            int next = (i + 1) % shapePoints.length;
            g.drawLine(centerX + shapePoints[i][0], centerY + shapePoints[i][1],
                      centerX + shapePoints[next][0], centerY + shapePoints[next][1]);
        }
        
        g.fillRect(centerX-2, centerY-2, 4, 4);
        g.fillRect(centerX+shapePoints[2][0]/2, centerY+shapePoints[2][1]/2, 3, 3);
        g.fillRect(centerX+shapePoints[5][0]/2, centerY+shapePoints[5][1]/2, 3, 3);
    }
}

class UFO extends SpaceObject {
    int dir = 1;
    
    UFO(double x, double y) {
        super(x, y, 40, 20);
        this.vx = 1.5;
        this.color = Color.RED;
    }
    
    void move() {
        x += vx * dir * 0.8;
        
        if (x < 20 || x > 760) {
            dir = -dir;
        }
        
        if (y < 20) y = 20;
        if (y > 580) y = 580;
    }
    
    void draw(Graphics2D g) {
        int drawX = (int)x;
        int drawY = (int)y;
        
        g.setColor(color);
        for (int i = 0; i < 3; i++) {
            g.fillRect(drawX + i*5, drawY, 4, 4);
            g.fillRect(drawX + i*5, drawY+4, 4, 4);
        }
        
        g.fillRect(drawX+5, drawY+8, 10, 6);
        
        g.setColor(Color.GREEN);
        g.fillRect(drawX+10, drawY+4, 3, 3);
        g.fillRect(drawX+20, drawY+4, 3, 3);
    }
}

class PlayerShip extends SpaceObject {
    int health = 3;
    int blinkTimer = 0;
    int shipType = 0;
    double speed = 2.5;
    
    PlayerShip(double x, double y, int shipType) {
        super(x, y, 30, 30);
        this.shipType = shipType;
        this.color = Color.GREEN;
    }
    
    void move() {
        x += vx;
        y += vy;
        
        if (x < 0) { x = 0; vx = 0; }
        if (x > 770) { x = 770; vx = 0; }
        if (y < 0) { y = 0; vy = 0; }
        if (y > 570) { y = 570; vy = 0; }
        
        if (blinkTimer > 0) blinkTimer--;
    }
    
    void draw(Graphics2D g) {
        if (blinkTimer > 0 && (blinkTimer/4) % 2 == 0) return;
        
        int drawX = (int)x;
        int drawY = (int)y;
        
        g.setColor(color);

        if (shipType == 0) {
            int[] xPts = {drawX + 15, drawX, drawX + 30};
            int[] yPts = {drawY, drawY + 30, drawY + 30};
            g.fillPolygon(xPts, yPts, 3);

            g.setColor(Color.CYAN);
            g.fillRect(drawX + 13, drawY + 8, 4, 8);

            g.setColor(Color.ORANGE);
            g.fillRect(drawX + 12, drawY + 28, 6, 4);
        } 
        else if (shipType == 1) {
            g.fillRect(drawX + 10, drawY + 10, 10, 10);
            g.fillRect(drawX,      drawY + 13, 30, 4);
            g.fillRect(drawX + 13, drawY,      4, 30);

            g.setColor(Color.ORANGE);
            g.fillRect(drawX + 3,  drawY + 25, 4, 4);
            g.fillRect(drawX + 23, drawY + 25, 4, 4);
        } 
        else {
            g.fillRect(drawX + 5, drawY + 8, 20, 14);
            g.fillRect(drawX,     drawY + 10, 5, 10);
            g.fillRect(drawX+25,  drawY + 10, 5, 10);

            g.setColor(Color.ORANGE);
            g.fillRect(drawX + 10, drawY + 22, 4, 6);
            g.fillRect(drawX + 16, drawY + 22, 4, 6);
        }
    }
    
    void hit() {
        health--;
        blinkTimer = 36;
    }
}

class Laser extends SpaceObject {
    int life = 30;
    boolean isPlayerLaser = true;
    
    Laser(double x, double y, double vx, double vy, boolean isPlayerLaser) {
        super(x, y, 3, 15);
        this.vx = vx * 1.2;
        this.vy = vy * 1.2;
        this.isPlayerLaser = isPlayerLaser;
        this.color = isPlayerLaser ? Color.CYAN : Color.RED;
    }
    
    void move() {
        x += vx;
        y += vy;
        life--;
        if (life <= 0) alive = false;
    }
    
    void draw(Graphics2D g) {
        int drawX = (int)x;
        int drawY = (int)y;
        
        if (isPlayerLaser) {
            g.setColor(color);
            g.fillRect(drawX, drawY, w, h);
            g.setColor(Color.WHITE);
            g.fillRect(drawX+1, drawY+1, w-2, h-2);
            
            if (life % 4 < 2) {
                g.setColor(new Color(0, 255, 255, 100));
                g.fillRect(drawX-1, drawY, w+2, h);
            }
        } else {
            g.setColor(color);
            g.fillRect(drawX, drawY, w, h);
        }
    }
}


class PowerUp extends SpaceObject {
    int type;
    int pulse = 0;
    
    PowerUp(double x, double y, int type) {
        super(x, y, 20, 20);
        this.type = type;
        this.vy = 1.2;
        
        if (type == 0) {
            color = Color.CYAN;
        } else if (type == 1) {
            color = Color.MAGENTA;
        } else {
            color = Color.YELLOW;
        }
    }
    
    void move() {
        y += vy;
        pulse = (pulse + 1) % 60;
        if (y > 600) alive = false;
    }
    
    void draw(Graphics2D g) {
        int drawX = (int)x;
        int drawY = (int)y;
        
        int size = 16 + (int)(4 * Math.sin(pulse * 0.1));
        
        g.setColor(color);
        int[] xPoints = {drawX+size/2, drawX+size, drawX+size/2, drawX};
        int[] yPoints = {drawY, drawY+size/2, drawY+size, drawY+size/2};
        g.fillPolygon(xPoints, yPoints, 4);
        
        g.setColor(Color.BLACK);
        if (type == 0) {
            g.drawString("S", drawX+7, drawY+13);
        } else if (type == 1) {
            g.drawString("F", drawX+7, drawY+13);
        } else {
            g.drawString("P", drawX+7, drawY+13);
        }
    }
}


class GamePanel extends JPanel implements Runnable, KeyListener {
    GameFrame parent;
    Thread gameThread;
    volatile boolean running = false;
    PlayerShip player;
    java.util.List<SpaceObject> asteroids = new ArrayList<>();
    java.util.List<SpaceObject> ufos = new ArrayList<>();
    java.util.List<Laser> lasers = new ArrayList<>();
    java.util.List<PowerUp> powerups = new ArrayList<>();
    
    int score = 0;
    int mapIndex = 0;
    BufferedImage bgImage;
    int tick = 0;
    boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false, spacePressed = false;
    Font retro = new Font("Monospaced", Font.BOLD, 14);
    Random rnd = new Random();
    String playerName;
    int shipChoice = 0;
    
    java.util.List<int[]> stars = new ArrayList<>();
    java.util.List<int[]> explosions = new ArrayList<>();

    long lastShotTime = 0;

   
    private javax.swing.Timer focusTimer;
    
    GamePanel(GameFrame p) {
        this.parent = p;
        setFocusable(true);
        setFocusTraversalKeysEnabled(false); // get arrow keys to work
        setBackground(Color.BLACK);
        addKeyListener(this);
        
        for (int i = 0; i < 150; i++) {
            stars.add(new int[]{rnd.nextInt(800), rnd.nextInt(600), rnd.nextInt(3)+1});
        }

        // again making sure the game window has the key 'focus' instead of the console below
        focusTimer = new javax.swing.Timer(200, e -> {
            if (running && !isFocusOwner()) {
                requestFocusInWindow();
            }
        });
        focusTimer.setRepeats(true);
    }

    void setup(int map, int ship, String user){
        this.mapIndex = map;
        this.shipChoice = ship;
        this.playerName = user;
        this.score = 0;
        asteroids.clear(); 
        ufos.clear();
        lasers.clear();
        powerups.clear();
        explosions.clear();
        
        player = new PlayerShip(400, 300, shipChoice);
        player.color = Color.GREEN;
        
        for (int i = 0; i < 6; i++) {
            double x = rnd.nextDouble() * 800;
            double y = rnd.nextDouble() * 600;
            double vx = (rnd.nextDouble() - 0.5) * 1.5;
            double vy = (rnd.nextDouble() - 0.5) * 1.5;
            asteroids.add(new Asteroid(x, y, rnd.nextInt(3), vx, vy));
        }
        
        bgImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bgImage.createGraphics();
        
        if (map == 0) {
            g.setColor(new Color(0, 0, 10));
            g.fillRect(0, 0, 800, 600);
        } else if (map == 1) {
            g.setColor(new Color(20, 0, 30));
            g.fillRect(0, 0, 800, 600);
            g.setColor(new Color(80, 0, 120, 80));
            for (int i = 0; i < 15; i++) {
                int size = 50 + rnd.nextInt(50);
                g.fillOval(rnd.nextInt(800), rnd.nextInt(600), size, size);
            }
        } else {
            g.setColor(new Color(15, 15, 25));
            g.fillRect(0, 0, 800, 600);
            g.setColor(new Color(80, 80, 80, 100));
            for (int i = 0; i < 25; i++) {
                int size = 10 + rnd.nextInt(20);
                g.fillOval(rnd.nextInt(800), rnd.nextInt(600), size, size);
            }
        }
        
        g.dispose();

        // key 'focus' on game start
        requestFocusInWindow();
    }

    void start() {
        running = true;
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
        // key 'focus' again
        focusTimer.start();
        requestFocusInWindow();
    }

    void stop() {
        running = false;
        focusTimer.stop();
        try { if (gameThread != null) gameThread.join(); } catch (InterruptedException ignored) {}
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        final double nsPerTick = 1000000000.0 / 60.0;
        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - last) / nsPerTick;
            last = now;
            boolean shouldRepaint = false;
            while (delta >= 1) {
                updateGame();
                delta--;
                shouldRepaint = true;
            }
            if (shouldRepaint) repaint();
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        }
    }

    void shootLaser() {
        if (!running) return;
        
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastShotTime < 250) {
            return;
        }
        lastShotTime = currentTime;
        
        double laserX = player.x + player.w/2 - 1.5;
        double laserY = player.y - 10;
        
        Laser laser = new Laser(laserX, laserY, 0, -8, true);
        lasers.add(laser);
        
        if (shipChoice == 1) {
            lasers.add(new Laser(laserX - 10, laserY, 0, -8, true));
            lasers.add(new Laser(laserX + 10, laserY, 0, -8, true));
        }
    }

    void updateGame() {
        tick++;
        
        player.vx = 0;
        player.vy = 0;
        
        if (leftPressed) player.vx = -player.speed;
        if (rightPressed) player.vx = player.speed;
        if (upPressed) player.vy = -player.speed;
        if (downPressed) player.vy = player.speed;
        
        if (spacePressed) {
            shootLaser();
        }
        
        player.move();
        
        if (tick % 400 == 0 && ufos.size() < 2) {
            UFO ufo = new UFO(rnd.nextDouble() * 800, rnd.nextDouble() * 200 + 50);
            ufos.add(ufo);
        }
        
        if (ufos.size() > 0 && rnd.nextInt(100) == 0) {
            UFO ufo = (UFO) ufos.get(rnd.nextInt(ufos.size()));
            lasers.add(new Laser(ufo.x + ufo.w/2, ufo.y + ufo.h, 0, 4, false));
        }
        
        if (tick % 600 == 0 && powerups.size() < 1) {
            int type = rnd.nextInt(3);
            powerups.add(new PowerUp(rnd.nextDouble() * 760, -20, type));
        }
        
        for (int i = 0; i < asteroids.size(); i++) {
            SpaceObject a = asteroids.get(i);
            a.move();
        }
        
        for (int i = 0; i < ufos.size(); i++) {
            SpaceObject u = ufos.get(i);
            u.move();
        }
        
        for (int i = 0; i < lasers.size(); i++) {
            Laser l = lasers.get(i);
            l.move();
            if (!l.alive || l.x < -20 || l.x > 820 || l.y < -20 || l.y > 620) {
                lasers.remove(i);
                i--;
            }
        }
        
        for (int i = 0; i < powerups.size(); i++) {
            PowerUp p = powerups.get(i);
            p.move();
            if (!p.alive) {
                powerups.remove(i);
                i--;
            }
        }
        
        for (int i = 0; i < lasers.size(); i++) {
            Laser l = lasers.get(i);
            if (!l.isPlayerLaser) continue;
            
            for (int j = 0; j < asteroids.size(); j++) {
                Asteroid a = (Asteroid) asteroids.get(j);
                
                if (l.collidesWith(a)) {
                    explosions.add(new int[]{(int)a.x, (int)a.y, 10});
                    
                    lasers.remove(i);
                    asteroids.remove(j);
                    
                    score += a.size == 0 ? 100 : a.size == 1 ? 200 : 300;
                    
                    if (a.size < 2) {
                        for (int k = 0; k < 2; k++) {
                            double newVx = (rnd.nextDouble() - 0.5) * 3;
                            double newVy = (rnd.nextDouble() - 0.5) * 3;
                            asteroids.add(new Asteroid(a.x, a.y, a.size + 1, newVx, newVy));
                        }
                    }
                    
                    i--;
                    break;
                }
            }
        }
        
        for (int i = 0; i < lasers.size(); i++) {
            Laser l = lasers.get(i);
            if (!l.isPlayerLaser) continue;
            
            for (int j = 0; j < ufos.size(); j++) {
                UFO u = (UFO) ufos.get(j);
                
                if (l.collidesWith(u)) {
                    explosions.add(new int[]{(int)u.x, (int)u.y, 15});
                    lasers.remove(i);
                    ufos.remove(j);
                    score += 500;
                    i--;
                    break;
                }
            }
        }
        
        for (int i = 0; i < asteroids.size(); i++) {
            Asteroid a = (Asteroid) asteroids.get(i);
            if (player.collidesWith(a) && player.blinkTimer == 0) {
                player.hit();
                explosions.add(new int[]{(int)player.x, (int)player.y, 8});
                if (player.health <= 0) {
                    gameOver();
                    return;
                }
            }
        }
        
        for (int i = 0; i < ufos.size(); i++) {
            UFO u = (UFO) ufos.get(i);
            if (player.collidesWith(u) && player.blinkTimer == 0) {
                player.hit();
                explosions.add(new int[]{(int)player.x, (int)player.y, 8});
                if (player.health <= 0) {
                    gameOver();
                    return;
                }
            }
        }
        
        for (int i = 0; i < lasers.size(); i++) {
            Laser l = lasers.get(i);
            if (l.isPlayerLaser) continue;
            
            if (player.collidesWith(l) && player.blinkTimer == 0) {
                player.hit();
                lasers.remove(i);
                explosions.add(new int[]{(int)player.x, (int)player.y, 8});
                i--;
                if (player.health <= 0) {
                    gameOver();
                    return;
                }
            }
        }
        
        for (int i = 0; i < powerups.size(); i++) {
            PowerUp p = powerups.get(i);
            if (player.collidesWith(p)) {
                if (p.type == 0) {
                    player.blinkTimer = 180;
                } else if (p.type == 1) {
                    score += 300;
                } else {
                    score += 500;
                }
                powerups.remove(i);
                i--;
            }
        }
        
        for (int i = 0; i < explosions.size(); i++) {
            explosions.set(i, new int[]{explosions.get(i)[0], explosions.get(i)[1], explosions.get(i)[2] - 1});
            if (explosions.get(i)[2] <= 0) {
                explosions.remove(i);
                i--;
            }
        }
        
        if (asteroids.size() < 4 && tick % 200 == 0) {
            double x = rnd.nextDouble() * 800;
            double y = rnd.nextDouble() * 600;
            double vx = (rnd.nextDouble() - 0.5) * 1.5;
            double vy = (rnd.nextDouble() - 0.5) * 1.5;
            asteroids.add(new Asteroid(x, y, rnd.nextInt(3), vx, vy));
        }
        
        if (tick % 20 == 0) {
            score++;
        }
    }
    
    void gameOver() {
        running = false;
        focusTimer.stop();
        parent.userData.saveScore(playerName, score);
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "GAME OVER\nFinal Score: " + score);
            parent.showMenu();
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        
        g.drawImage(bgImage, 0, 0, null);
        
        g.setColor(Color.WHITE);
        for (int[] star : stars) {
            g.fillRect(star[0], star[1], star[2], star[2]);
        }
        
        for (SpaceObject a : asteroids) a.draw(g);
        for (SpaceObject u : ufos) u.draw(g);
        for (Laser l : lasers) l.draw(g);
        for (PowerUp p : powerups) p.draw(g);
        
        player.draw(g);
        
        for (int[] exp : explosions) {
            g.setColor(Color.ORANGE);
            g.fillOval(exp[0]-exp[2], exp[1]-exp[2], exp[2]*2, exp[2]*2);
            g.setColor(Color.YELLOW);
            g.fillOval(exp[0]-exp[2]/2, exp[1]-exp[2]/2, exp[2], exp[2]);
        }
        
        g.setColor(new Color(0, 255, 255, 150));
        g.fillRect(5, 5, 200, 70);
        g.setColor(Color.BLACK);
        g.fillRect(10, 10, 190, 60);
        
        g.setColor(Color.WHITE);
        g.setFont(retro);
        g.drawString("SCORE: " + score, 20, 30);
        g.drawString("HEALTH: " + player.health, 20, 50);
        g.drawString("ASTEROIDS: " + asteroids.size(), 120, 30);
        
        g.setColor(new Color(0, 255, 255, 150));
        g.fillRect(595, 5, 200, 70);
        g.setColor(Color.BLACK);
        g.fillRect(600, 10, 190, 60);
        
        g.setColor(Color.WHITE);
        g.drawString("MAP: " + (mapIndex == 0 ? "DEEP SPACE" : mapIndex == 1 ? "NEBULA" : "BELT"), 610, 30);
        g.drawString("SHIP: " + (shipChoice == 0 ? "FIGHTER" : shipChoice == 1 ? "INTERCEPTOR" : "BOMBER"), 610, 50);
        
        g.setColor(Color.GRAY);
        g.drawString("CONTROLS: ARROWS=MOVE, SPACE=SHOOT, ESC=MENU", 10, 585);
        
        g.setColor(new Color(0, 0, 0, 30));
        for (int y = 0; y < 600; y += 3) {
            g.fillRect(0, y, 800, 1);
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT) leftPressed = true;
        if (k == KeyEvent.VK_RIGHT) rightPressed = true;
        if (k == KeyEvent.VK_UP) upPressed = true;
        if (k == KeyEvent.VK_DOWN) downPressed = true;
        if (k == KeyEvent.VK_SPACE) spacePressed = true;
        if (k == KeyEvent.VK_ESCAPE) { running = false; focusTimer.stop(); parent.showMenu(); }
    }
    
    @Override public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_LEFT) leftPressed = false;
        if (k == KeyEvent.VK_RIGHT) rightPressed = false;
        if (k == KeyEvent.VK_UP) upPressed = false;
        if (k == KeyEvent.VK_DOWN) downPressed = false;
        if (k == KeyEvent.VK_SPACE) spacePressed = false;
    }
    
    @Override public void keyTyped(KeyEvent e) {}
}
