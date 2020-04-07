package ml.yike.yueyin.entity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;


public class MusicInfo implements Comparable, Parcelable {


    private int id;

    private String name;

    private String singer;

    private String album;

    private int album_id;

    private String duration;

    private String path;

    private String parentPath; //父目录路径

    private Uri albumUri;              //存储音乐封面的Uri地址

    public Bitmap thumb; //存储封面图片

    private int love; //1设置我喜欢 0未设置

    private String firstLetter;


    public String getAlbum() {
        return album;
    }

    public Uri getalbumUri() {
        return albumUri;
    }

    public int getAlbumId() {
        return album_id;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
    public void setAlbumUri(Uri albumUri) {
        this.albumUri = albumUri;
    }
    public void setAlbumId(int album_id) {
        this.album_id = album_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * 为了能够进行排序
     */
    @Override
    public int compareTo(Object o) {
        MusicInfo info = (MusicInfo) o;
        if (info.getFirstLetter().equals("#")) return -1;
        if (firstLetter.equals("#")) return 1;
        return this.firstLetter.compareTo(info.getFirstLetter());
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", duration='" + duration + '\'' +
                ", path='" + path + '\'' +
                ", parentPath='" + parentPath + '\'' +
                ", love=" + love +
                ", firstLetter='" + firstLetter + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.singer);
        dest.writeString(this.album);
        dest.writeString(this.duration);
        dest.writeString(this.path);
        dest.writeString(this.parentPath);
        dest.writeInt(this.love);
        dest.writeString(this.firstLetter);
    }

    public MusicInfo() {
    }

    protected MusicInfo(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.singer = in.readString();
        this.album = in.readString();
        this.duration = in.readString();
        this.path = in.readString();
        this.parentPath = in.readString();
        this.love = in.readInt();
        this.firstLetter = in.readString();
    }

    public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel source) {
            return new MusicInfo(source);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };


}

