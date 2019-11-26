package tera.aiwolf.util;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import tera.aiwolf.framework.GameAgent;

public class Utils {

    /**
     * 最もスコアの高いものを取得する。複数同着の場合には全て返す
     */
    public static <E> List<E> getHighestScores(List<E> list, Function<E, Double> f) {
        double max = Double.NEGATIVE_INFINITY;
        List<E> result = new ArrayList<>();
        for (E e : list) {
            double score = f.apply(e);
            if (score > max) {
                max = score;
                result.clear();
                result.add(e);
            } else if (score == max) {
                result.add(e);
            }
        }
        return result;
    }

    static Random rand = new SecureRandom();

    public static <T extends Object> T getRandom(List<T> list) {
        if (list.isEmpty()) return null;
        else return list.get(rand.nextInt(list.size()));
    }

    //TODO ここをfalseにすると、ログが出力される。
    private static boolean IS_Production = true;

    public static void log(Object... logs) {
        if (!IS_Production) {
            StackTraceElement ste[] = Thread.currentThread().getStackTrace();
            String clazz = "UK";
            if (ste.length > 2) {
                clazz = ste[2].getClassName().replaceAll("^.+\\.", "") + "-" + ste[2].getMethodName();
            }
            System.out.println("*LOG*\t" + clazz + "\t" + Arrays.stream(logs).map(o -> Objects.toString(o)).collect(Collectors.joining("\t")));
        }
    }

    public static String toString(Object obj) {
        List<String> values = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object val = f.get(obj);
                if (val != null) {
                    values.add(f.getName() + "=" + Objects.toString(val));
                }
            } catch (Exception e) {
            }
        }
        return obj.getClass().getSimpleName() + " {" + String.join(", ", values) + "}";
    }

    public static void sortByScore(List<GameAgent> agents, double[] score, boolean asc) {
        agents.sort(Comparator.comparing((GameAgent ag) -> (asc ? 1 : -1) * score[ag.getIndex()]));
    }
}
