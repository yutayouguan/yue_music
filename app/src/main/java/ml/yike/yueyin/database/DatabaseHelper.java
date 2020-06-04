package ml.yike.yueyin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库工具类
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    /**
     * 数据库的名字
     */
    private static final String DATABASE_NAME = "YueMusicDatabase.db";

    /**
     * 数据库版本号
     */
    private static final int VERSION = 2;

    public static final String ID_COLUMN = "id";
    public static final String MUSIC_ID_COLUMN = "music_id";
    public static final String NAME_COLUMN = "name";
    public static final String SINGER_COLUMN = "singer";
    public static final String DURATION_COLUMN = "duration";
    public static final String ALBUM_COLUMN = "album";
    public static final String ALBUM_ID_COLUMN = "album_id";
    public static final String PATH_COLUMN = "path";
    public static final String PARENT_PATH_COLUMN = "parent_path";
    public static final String FIRST_LETTER_COLUMN = "first_letter";
    public static final String LOVE_COLUMN = "love";

    /**
     * 音乐表
     */
    public static final String MUSIC_TABLE = "yuemusic_table";

    /**
     * 建立音乐表语句
     */
    private String createMusicTable = "create table if not exists " + MUSIC_TABLE + "("
            + ID_COLUMN + " integer PRIMARY KEY ,"
            + NAME_COLUMN + " text,"
            + SINGER_COLUMN + " text,"
            + ALBUM_COLUMN + " text,"
			+ ALBUM_ID_COLUMN + " integer,"
            + DURATION_COLUMN + " long,"
            + PATH_COLUMN + " text,"
            + PARENT_PATH_COLUMN + " text,"
            + LOVE_COLUMN + " integer,"
            + FIRST_LETTER_COLUMN + " text );";


    /**
     * 最近播放
     */
    public static final String LAST_PLAY_TABLE = "last_play_table";

    /**
     * 创建最近播放表
     */
    private String createLastPlayTable = "create table if not exists " + LAST_PLAY_TABLE + " ("
            + ID_COLUMN + " integer,"
            + "FOREIGN KEY(id) REFERENCES "
            + MUSIC_TABLE
            + " (id) ON DELETE CASCADE);";

    /**
     * 歌单表
     */
    public static final String PLAY_LIST_TABLE = "play_list_table";


    /**
     * 创建歌单表
     */
    private String createPlaylistTable = "create table if not exists " + PLAY_LIST_TABLE + " ("
            + ID_COLUMN
            + " integer PRIMARY KEY autoincrement,"
            + NAME_COLUMN + " text);";

    /**
     * 歌单歌曲表
     */
    public static final String PLAY_LISY_MUSIC_TABLE = "play_list_music_table";


    /**
     * 创建歌单歌曲表
     */
    private String createListinfoTable = "create table if not exists " + PLAY_LISY_MUSIC_TABLE + " ("
            + ID_COLUMN + " integer,"
            + MUSIC_ID_COLUMN + " integer,"
            + "FOREIGN KEY(id) REFERENCES " + PLAY_LIST_TABLE + "(id) ON DELETE CASCADE,"
            + "FOREIGN KEY(music_id) REFERENCES " + MUSIC_TABLE + " (id) ON DELETE CASCADE) ;";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createMusicTable);  //创建音乐表
        db.execSQL(createLastPlayTable);  //创建播放历史表
        db.execSQL(createPlaylistTable);  //创建歌单表
        db.execSQL(createListinfoTable);  //创建歌单歌曲表
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < VERSION) {
            //如果遇到数据库更新，我们简单的处理为删除以前的表，重新创建一张
            db.execSQL("drop table if exists " + MUSIC_TABLE);
            db.execSQL("drop table if exists " + LAST_PLAY_TABLE);
            db.execSQL("drop table if exists " + PLAY_LIST_TABLE);
            db.execSQL("drop table if exists " + PLAY_LISY_MUSIC_TABLE);
            this.onCreate(db);
        }
    }
}
