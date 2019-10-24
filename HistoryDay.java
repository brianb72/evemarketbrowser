package com.troff.evemarketbrowser;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* Structure of history file
{"totalCount_str": "398",
"items": [
{"volume_str": "4487416", "orderCount": 266, "lowPrice": 595.07, "highPrice": 618.57, "avgPrice": 595.14, "volume": 4487416, "orderCount_str": "266", "date": "2015-05-01T00:00:00"},
{"volume_str": "7417912", "orderCount": 261, "lowPrice": 595.0, "highPrice": 595.0, "avgPrice": 595.0, "volume": 7417912, "orderCount_str": "261", "date": "2015-05-02T00:00:00"},
...
]}

Volume and orderCount are given as a string and an integer, ignore the "xxx_str" fields and only
keep the integers.

 */



// All setters will throw NumberformatException.
// setDate(string) catches the ParseException and rethrows a NumberFormatException

public class HistoryDay implements Comparable<HistoryDay>, Parcelable {
    Date date;
    long volume;
    long orderCount;
    BigDecimal lowPrice;
    BigDecimal highPrice;
    BigDecimal avgPrice;


    // Parcelable
    public static final Parcelable.Creator<HistoryDay> CREATOR = new Parcelable.Creator<HistoryDay>() {
        public HistoryDay createFromParcel(Parcel in) {
            return new HistoryDay(in);
        }
        public HistoryDay[] newArray(int size) {
            return new HistoryDay[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date.getTime());
        dest.writeLong(volume);
        dest.writeLong(orderCount);
        dest.writeString(lowPrice.toString());
        dest.writeString(highPrice.toString());
        dest.writeString(avgPrice.toString());
    }

    private HistoryDay(Parcel in) {
        date = new Date(in.readLong());
        volume = in.readLong();
        orderCount = in.readLong();
        lowPrice = new BigDecimal(in.readString());
        highPrice = new BigDecimal(in.readString());
        avgPrice = new BigDecimal(in.readString());
    }


    public HistoryDay(Date date, long volume, long orderCount, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal avgPrice) {
        this.date = date;
        this.volume = volume;
        this.orderCount = orderCount;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.avgPrice = avgPrice;
    }

    public HistoryDay(String date, long volume, long orderCount, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal avgPrice) {
        this.setDate(date);
        this.volume = volume;
        this.orderCount = orderCount;
        this.lowPrice = lowPrice;
        this.highPrice = highPrice;
        this.avgPrice = avgPrice;
    }



    // date
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            NumberFormatException nfe = new NumberFormatException(e.getLocalizedMessage());
            nfe.initCause(e);
            throw nfe;
        }
    }


    // volume
    public long getVolume() {
        return volume;
    }
    public void setVolume(long volume) {
        this.volume = volume;
    }
    // orderCount
    public long getOrderCount() {
        return orderCount;
    }
    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }
    // lowPrice
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }
    // highPrice
    public BigDecimal getHighPrice() {
        return highPrice;
    }
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
    // avgPrice
    public BigDecimal getAvgPrice() {
        return avgPrice;
    }
    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }



    @Override
    // Natural ordering is price least to greatest.
    public int compareTo(HistoryDay o) {
        return this.date.compareTo(o.date);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryDay that = (HistoryDay) o;

        if (volume != that.volume) return false;
        if (orderCount != that.orderCount) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (lowPrice != null ? !lowPrice.equals(that.lowPrice) : that.lowPrice != null)
            return false;
        if (highPrice != null ? !highPrice.equals(that.highPrice) : that.highPrice != null)
            return false;
        return avgPrice != null ? avgPrice.equals(that.avgPrice) : that.avgPrice == null;

    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (int) (volume ^ (volume >>> 32));
        result = 31 * result + (int) (orderCount ^ (orderCount >>> 32));
        result = 31 * result + (lowPrice != null ? lowPrice.hashCode() : 0);
        result = 31 * result + (highPrice != null ? highPrice.hashCode() : 0);
        result = 31 * result + (avgPrice != null ? avgPrice.hashCode() : 0);
        return result;
    }
}
