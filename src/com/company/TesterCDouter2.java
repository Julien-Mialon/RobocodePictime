package com.company;

import robocode.Robot;
import robocode.*;

import java.awt.*;
import java.util.Random;

public class TesterCDouter2 extends Robot
{
    private int _robotCount;
    private double _size;
    private double _maxFireDistance;

    public void run()
    {
        setBodyColor(Color.red);
        setGunColor(Color.blue);
        setRadarColor(Color.green);
        setBulletColor(Color.red);
        setScanColor(Color.red);

        _robotCount = getOthers();
        _size = Math.max(getBattleFieldHeight(), getBattleFieldWidth());
        _maxFireDistance = _size * 0.4;

        Random random = new Random(System.nanoTime());

        while (true)
        {

            ahead(200); // Move ahead 10

            Turn(random);
            /*
            int angle = (int)((random.nextInt(40) - 20) * 1.5);

            if(angle < 0)
            {
                turnLeft( -angle);
            }
            else
            {
                turnRight(angle);
            }
             */
        }
    }

    private void Turn(Random rand)
    {
        var height = getBattleFieldHeight();
        var width = getBattleFieldWidth();
        var paddingHeight = height * 0.2;
        var paddingWidth = width * 0.2;
        var distHeight = height * 0.7;
        var distWidth = width * 0.7;

        int dir;
        if (getX() < paddingWidth) {
            dir = pick(rand , 0, 180, 90);
        } else if (getX() > width - paddingWidth) {
            dir = pick(rand , 0, 180, 270);
        } else if (getY() < paddingHeight) {
            dir = pick(rand , 0, 90, 270);
        } else if (getY() > height - paddingHeight) {
            dir = pick(rand , 270, 90, 180);
        }
        else {
            return;
            //dir = pick(rand , 0, 180, 90, 270);
        }

        double relativeHeading = ((dir - getHeading()) + 180) % 360 - 180;
        if(relativeHeading < 0)
        {
            turnLeft( -relativeHeading);
        }
        else
        {
            turnRight(relativeHeading);
        }

    }

    private static int pick(Random rand, int... possible) {
        var index = rand.nextInt(possible.length);
        return possible[index];
    }

    public void onBulletHit(BulletHitEvent event) {
    }

    public void onBulletHitBullet(BulletHitBulletEvent event) {
    }

    public void onBulletMissed(BulletMissedEvent event) {
    }

    public void onHitByBullet(HitByBulletEvent event)
    {
        if(event.getBearing() < 0)
        {
            turnRight(30);
        }
        else
        {
            turnLeft(30);
        }
    }

    public void onHitRobot(HitRobotEvent event) {

        ShootToRobot(event.getBearing());
    }



    public void onHitWall(HitWallEvent event) {
        turnLeft(90);
    }

    public void onRobotDeath(RobotDeathEvent event) {
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        ShootToRobot(event.getDistance(), event.getVelocity(), event.getBearingRadians(), event.getHeadingRadians(), event.getBearing());
    }


    public void onRoundEnded(RoundEndedEvent event) {
    }

    public void onBattleEnded(BattleEndedEvent event) {
    }

    public void onWin(WinEvent event) {
    }

    public void onDeath(DeathEvent event) {
    }

    private double GetPowerForBullet(double energy, boolean close)
    {
        if(energy < 20)
        {
            return 0;
        }

        if(energy < 40)
        {
            return close ? 3 : 1;
        }

        if(energy < 60)
        {
            return close ? 4 : 2;
        }

        if(energy < 80)
        {
            return close ? 5 : 3;
        }

        return close ? 6 : 3;
    }

    private void ShootToRobot(double distance, double velocity, double bearingRadians, double headingRadians, double bearing)
    {
        if(!CanShoot() || distance > _maxFireDistance)
        {
            return;
        }

        double enemyX = getX() + distance * Math.cos(bearingRadians);
        double enemyY = getY() + distance * Math.sin(bearingRadians);

        double velocityX = velocity * Math.cos(headingRadians);
        double velocityY = velocity * Math.sin(headingRadians);

        double power = GetPowerForBullet(getEnergy(), false);
        double bulletSpeed = Rules.getBulletSpeed(power);

        int inc = bearing < 0 ? -1 : 1;


        double enemyCoefA = FindA(enemyX, enemyY, enemyX + velocityX, enemyY + velocityY);
        double enemyCoefB = FindB(enemyCoefA, enemyX, enemyY);

        for(int i = 0 ; i < 90 ; ++i)
        {
            double bulletVelocityX = bulletSpeed * Math.cos(bearing + i * inc);
            double bulletVelocityY = bulletSpeed * Math.sin(bearing + i * inc);

            double bulletCoefA = FindA(getX(), getY(), getX() + bulletVelocityX, getY() + bulletVelocityY);
            double bulletCoefB = FindB(bulletCoefA, getX(), getY());

            if(bulletCoefA == enemyCoefA)
            {
                continue;
            }

            double interceptionX = (bulletCoefB - enemyCoefB) / (enemyCoefA - bulletCoefA);
            double interceptionY = bulletCoefA * interceptionX + bulletCoefB;

            double distanceBullet = Distance(getX(), getY(), interceptionX, interceptionY);
            double distanceEnemy = Distance(enemyX, enemyY, interceptionX, interceptionY);

            double expectedDistance = velocity / bulletSpeed * distanceBullet;

            if(Math.abs(expectedDistance - distanceEnemy) < 1)
            {
                double fireHeading = (getHeading() + bearing + i * inc + 360) % 360;
                double currentBearing = getGunHeading();

                double moveHeading = ((fireHeading - currentBearing + 180) % 360) - 180;
                if(moveHeading < 0)
                {
                    turnGunLeft(-moveHeading);
                }
                else
                {
                    turnGunRight(moveHeading);
                }

                fire(power);
            }
        }

        if(bearing < 0)
        {
            turnRight(10);
        }
        else{
            turnLeft(10);
        }
    }

    private void ShootToRobot(double bearing)
    {
        if(!CanShoot())
        {
            return;
        }

        double fireHeading = getHeading() + bearing;
        double currentBearing = getGunHeading();

        double moveHeading = fireHeading - currentBearing;
        if(moveHeading < 0)
        {
            turnGunLeft(-moveHeading);
        }
        else
        {
            turnGunRight(moveHeading);
        }

        fire(GetPowerForBullet(getEnergy(), true));

        if(bearing < 0)
        {
            turnRight(10);
        }
        else{
            turnLeft(10);
        }
    }

    public double FindA(double x1, double y1, double x2, double y2)
    {
        return (y1 - y2) / (x1 - x2);
    }

    public double FindB(double a, double x1, double y1)
    {
        return y1 - a * x1;
    }

    public double Distance(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private boolean CanShoot()
    {
        return getGunHeat() < 2;
    }

    private class RobotDev extends Robot
    {
        private int _robotCount;
        private double _size;
        private double _maxFireDistance;

        public void run()
        {
            _robotCount = getOthers();
            _size = Math.max(getBattleFieldHeight(), getBattleFieldWidth());
            _maxFireDistance = _size * 0.4;

            Random random = new Random(System.nanoTime());

            while (true)
            {

                ahead(Rules.MAX_VELOCITY * 100); // Move ahead 10

                int angle = (int)((random.nextInt(40) - 20) * 1.5);

                if(angle < 0)
                {
                    turnLeft( -angle);
                }
                else
                {
                    turnRight(angle);
                }
            }
        }

        public void onBulletHit(BulletHitEvent event) {
    }

        public void onBulletHitBullet(BulletHitBulletEvent event) {
    }

        public void onBulletMissed(BulletMissedEvent event) {
    }

        public void onHitByBullet(HitByBulletEvent event)
        {
            if(event.getBearing() < 0)
            {
                turnRight(30);
            }
            else
            {
                turnLeft(30);
            }
        }

        public void onHitRobot(HitRobotEvent event) {

        ShootToRobot(event.getBearing());
    }



        public void onHitWall(HitWallEvent event) {
        turnLeft(90);
    }

        public void onRobotDeath(RobotDeathEvent event) {
    }

        public void onScannedRobot(ScannedRobotEvent event) {
        ShootToRobot(event.getDistance(), event.getVelocity(), event.getBearingRadians(), event.getHeadingRadians(), event.getBearing());
    }


        public void onRoundEnded(RoundEndedEvent event) {
    }

        public void onBattleEnded(BattleEndedEvent event) {
    }

        public void onWin(WinEvent event) {
    }

        public void onDeath(DeathEvent event) {
    }

        private double GetPowerForBullet(double energy, boolean close)
        {
            if(energy < 20)
            {
                return 0;
            }

            if(energy < 40)
            {
                return close ? 3 : 1;
            }

            if(energy < 60)
            {
                return close ? 4 : 2;
            }

            if(energy < 80)
            {
                return close ? 5 : 3;
            }

            return close ? 6 : 3;
        }

        private void ShootToRobot(double distance, double velocity, double bearingRadians, double headingRadians, double bearing)
        {
            if(!CanShoot() || distance > _maxFireDistance)
            {
                return;
            }

            double enemyX = getX() + distance * Math.cos(bearingRadians);
            double enemyY = getY() + distance * Math.sin(bearingRadians);

            double velocityX = velocity * Math.cos(headingRadians);
            double velocityY = velocity * Math.sin(headingRadians);

            double power = GetPowerForBullet(getEnergy(), false);
            double bulletSpeed = Rules.getBulletSpeed(power);

            int inc = bearing < 0 ? -1 : 1;


            double enemyCoefA = FindA(enemyX, enemyY, enemyX + velocityX, enemyY + velocityY);
            double enemyCoefB = FindB(enemyCoefA, enemyX, enemyY);

            for(int i = 0 ; i < 90 ; ++i)
            {
                double bulletVelocityX = bulletSpeed * Math.cos(bearing + i * inc);
                double bulletVelocityY = bulletSpeed * Math.sin(bearing + i * inc);

                double bulletCoefA = FindA(getX(), getY(), getX() + bulletVelocityX, getY() + bulletVelocityY);
                double bulletCoefB = FindB(bulletCoefA, getX(), getY());

                if(bulletCoefA == enemyCoefA)
                {
                    continue;
                }

                double interceptionX = (bulletCoefB - enemyCoefB) / (enemyCoefA - bulletCoefA);
                double interceptionY = bulletCoefA * interceptionX + bulletCoefB;

                double distanceBullet = Distance(getX(), getY(), interceptionX, interceptionY);
                double distanceEnemy = Distance(enemyX, enemyY, interceptionX, interceptionY);

                double expectedDistance = velocity / bulletSpeed * distanceBullet;

                if(Math.abs(expectedDistance - distanceEnemy) < 1)
                {
                    double fireHeading = (getHeading() + bearing + i * inc + 360) % 360;
                    double currentBearing = getGunHeading();

                    double moveHeading = ((fireHeading - currentBearing + 180) % 360) - 180;
                    if(moveHeading < 0)
                    {
                        turnGunLeft(-moveHeading);
                    }
                    else
                    {
                        turnGunRight(moveHeading);
                    }

                    fire(power);
                }
            }

            if(bearing < 0)
            {
                turnRight(10);
            }
            else{
                turnLeft(10);
            }
        }

        private void ShootToRobot(double bearing)
        {
            if(!CanShoot())
            {
                return;
            }

            double fireHeading = getHeading() + bearing;
            double currentBearing = getGunHeading();

            double moveHeading = fireHeading - currentBearing;
            if(moveHeading < 0)
            {
                turnGunLeft(-moveHeading);
            }
            else
            {
                turnGunRight(moveHeading);
            }

            fire(GetPowerForBullet(getEnergy(), true));

            if(bearing < 0)
            {
                turnRight(10);
            }
            else{
                turnLeft(10);
            }
        }

        public double FindA(double x1, double y1, double x2, double y2)
        {
            return (y1 - y2) / (x1 - x2);
        }

        public double FindB(double a, double x1, double y1)
        {
            return y1 - a * x1;
        }

        public double Distance(double x1, double y1, double x2, double y2)
        {
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }

        private boolean CanShoot()
        {
            return getGunHeat() < 2;
        }
    }
}
