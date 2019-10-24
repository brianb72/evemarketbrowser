package com.troff.evemarketbrowser;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

public class MarketOrder implements Comparable<MarketOrder>, Parcelable {
    private BigDecimal price;
    private long vol_remain;

    public MarketOrder() {

    }

    public MarketOrder(BigDecimal price, long vol_remain) {
        this.price = price;
        this.vol_remain = vol_remain;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getVol_remain() {
        return vol_remain;
    }

    public void setVol_remain(long vol_remain) {
        this.vol_remain = vol_remain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarketOrder that = (MarketOrder) o;

        if (vol_remain != that.vol_remain) return false;
        return price != null ? price.equals(that.price) : that.price == null;

    }

    @Override
    public int hashCode() {
        int result = price != null ? price.hashCode() : 0;
        result = 31 * result + (int) (vol_remain ^ (vol_remain >>> 32));
        return result;
    }

    // Parcelable
    public static final Parcelable.Creator<MarketOrder> CREATOR = new Parcelable.Creator<MarketOrder>() {
        public MarketOrder createFromParcel(Parcel in) {
            return new MarketOrder(in);
        }
        public MarketOrder[] newArray(int size) {
            return new MarketOrder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(price.toString());
        dest.writeLong(vol_remain);
    }

    private MarketOrder(Parcel in) {
        price = new BigDecimal(in.readString());
        vol_remain = in.readLong();
    }

    @Override
    // Natural ordering is price least to greatest.
    public int compareTo(MarketOrder o) {
        return this.price.compareTo(o.price);
    }

    // REVERSE_PRICE sorts price greatest to least
    // To sort "List myList;" use "Collections.sort(myList, REVERSE_PRICE);"
    static final Comparator<MarketOrder> COMPARE_PRICE_LOWTOHIGH =
            new Comparator<MarketOrder>() {
                public int compare(MarketOrder o1, MarketOrder o2) {
                    return o1.getPrice().compareTo(o2.getPrice());
                }
            };

    static final Comparator<MarketOrder> COMPARE_PRICE_HIGHTOLOW =
            new Comparator<MarketOrder>() {
                public int compare(MarketOrder o1, MarketOrder o2) {
                    return o2.getPrice().compareTo(o1.getPrice());
                }
            };

}
