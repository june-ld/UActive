package com.junelabs.june.uactive;

/**
 * Created by June on 3/17/2016.
 */
public class XPConstants {

    public static int getLevel(int experience){

        int currentlevel;
        int levelcap = 0;

        for(currentlevel = 1; currentlevel < 11; currentlevel++){
            levelcap = levelcap + (currentlevel * 100);

            if(levelcap > experience)
                break;
        }
        return currentlevel;
    }

    public static int getToNext(int experience){
        int currentlevel;
        int levelcap = 0;

        for(currentlevel = 1; currentlevel < 11; currentlevel++){
            levelcap = levelcap + (currentlevel * 100);

            if(levelcap > experience)
                break;
        }

        return (levelcap - experience);
    }
}
