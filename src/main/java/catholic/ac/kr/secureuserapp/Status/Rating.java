package catholic.ac.kr.secureuserapp.Status;

import lombok.Getter;

@Getter
public enum Rating {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int value;

    Rating(int value) {
        this.value = value;
    }

    public static Rating getRating(int value) {
        return switch (value) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            case 5 -> FIVE;
            default -> null;
        };
    }

    public static int getValue(Rating rating) {
        return rating != null ? rating.getValue() : 0;
    }

}
