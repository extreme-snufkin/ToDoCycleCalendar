/**
 ToDoCycleCalendar
 Copyright (C) 2012  Tohru Mashiko

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.


 クラス名：GDataCalendarParser
 内容：Calendar GDATAのパーサ
 特記事項：特になし
 更新履歴(バージョン情報/年月日/氏名)：
			0.1/2012.06.27/T.Mashiko
			0.2/2012.06.28/T.Mashiko
			0.3/2012.06.29/T.Mashiko
			0.4/2012.06.30/T.Mashiko
			0.5/2012.07.02/T.Mashiko
			0.6/2012.07.03/T.Mashiko
			0.7/2012.07.04/T.Mashiko
			0.8/2012.09.06/T.Mashiko 同期通信時に更新データを送信し、正常コードが返ってきてもデータが更新されないケースの対策
			0.9/2012.09.10/T.Mashiko カレンダー側データより端末側データが新しい場合の更新フラグを有効にする処理に於いて更新日時をアップデートしていない件の対策
			1.0/2012.09.10/T.Mashiko 更新XML作成時に元データのイベントステータスを入れ込んでいたが、削除データを有効なデータとして更新できていなかった修正(更新時は常に有効なデータとして修正)
			1.1/2012.09.11/T.Mashiko カレンダー側で削除されたデータを対象に削除通信が発生するとHTTPステータス403エラーが発生することに対する対応
			1.2/2012.09.19/T.Mashiko 同期時のOutOfMemoryエラー対応
*/
package study.tdcc.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import study.tdcc.bean.GDataEvent;
import study.tdcc.bean.Schedule;

import android.app.Activity;
import android.content.Context;
import android.database.SQLException;
import android.util.Log;
import android.util.Xml;


public class GDataCalendarParser {
	//NameSpaceの定義
	public static final String NS_GD = "gd";
	public static final String NS_APP = "app";
	public static final String NS_XMLNS = "xmlns";
	public static final String NS_GCAL = "gCal";
	public static final String NS_OPEN_SEARCH = "openSearch";

	//feedで使用されるTAGの定義
	public static final String TAG_FEED = "feed";
	public static final String TAG_AUTHOR = "author";
	public static final String TAG_NAME = "name";
	public static final String TAG_GENERATOR = "generator";
	public static final String TAG_TOTAL_RESULT = "totalResult";
	public static final String TAG_START_INDEX = "startIndex";
	public static final String TAG_ITEM_PER_PAGE = "itemPerPage";
	public static final String TAG_TIMEZONE = "timezone";
	public static final String TAG_TIME_CLEANED = "timeCleaned";

	//entryで使用されるTAGの定義
	public static final String TAG_ENTRY = "entry";
	public static final String TAG_TEXT = "#text";
	public static final String TAG_ID = "id";
	public static final String TAG_EDITED = "edited";
	public static final String TAG_PUBLISHED = "published";
	public static final String TAG_UPDATED = "updated";
	public static final String TAG_CATEGORY = "category";
	public static final String TAG_TITLE = "title";
	public static final String TAG_CONTENT = "content";
	public static final String TAG_LINK = "link";
	public static final String TAG_WHERE = "where";
	public static final String TAG_WHO = "who";
	public static final String TAG_WHEN = "when";
	public static final String TAG_COMMENTS = "comments";
	public static final String TAG_EVENT_STATUS = "eventStatus";
	public static final String TAG_REMINDER = "reminder";
	public static final String TAG_TRANSPARENCY = "transparency";
	public static final String TAG_VISIBILITY = "visibility";
	public static final String TAG_ANYONE_CAN_ADD_SELF = "anyoneCanAddSelf";
	public static final String TAG_GUESTS_CAN_INVITE_OTHERS = "guestsCanInviteOthers";
	public static final String TAG_GUESTS_CAN_MODIFY = "guestsCanModify";
	public static final String TAG_GUESTS_CAN_SEE_GUESTS = "guestsCanSeeGuests";
	public static final String TAG_SEQUENCE = "sequence";
	public static final String TAG_UID = "uid";
	public static final String TAG_RECURRENCE = "recurrence";

	//NameSpace付きTAGの定義
	public static final String TAG_GD_WHERE = NS_GD+":"+TAG_WHERE;
	public static final String TAG_GD_WHO = NS_GD+":"+TAG_WHO;
	public static final String TAG_GD_WHEN = NS_GD+":"+TAG_WHEN;
	public static final String TAG_GD_REMINDER = NS_GD+":"+TAG_REMINDER;
	public static final String TAG_GD_COMMENTS = NS_GD+":"+TAG_COMMENTS;
	public static final String TAG_GD_EVENT_STATUS = NS_GD+":"+TAG_EVENT_STATUS;
	public static final String TAG_GD_TRANSPARENCY = NS_GD+":"+TAG_TRANSPARENCY;
	public static final String TAG_GD_VISIBILITY = NS_GD+":"+TAG_VISIBILITY;
	public static final String TAG_APP_EDITED = NS_APP+":"+TAG_EDITED;
	public static final String TAG_OPENSEARCH_TOTAL_RESULT = NS_OPEN_SEARCH+":"+TAG_TOTAL_RESULT;
	public static final String TAG_OPENSEARCH_START_INDEX = NS_OPEN_SEARCH+":"+TAG_START_INDEX;
	public static final String TAG_OPENSEARCH_ITEM_PER_PAGE = NS_OPEN_SEARCH+":"+TAG_ITEM_PER_PAGE;
	public static final String TAG_GCAL_TIMEZONE = NS_GCAL+":"+TAG_TIMEZONE;
	public static final String TAG_GCAL_TIME_CLEANED = NS_GCAL+":"+TAG_TIME_CLEANED;
	public static final String TAG_GCAL_ANYONE_CAN_ADD_SELF = NS_GCAL+":"+TAG_ANYONE_CAN_ADD_SELF;
	public static final String TAG_GCAL_GUESTS_CAN_INVITE_OTHERS = NS_GCAL+":"+TAG_GUESTS_CAN_INVITE_OTHERS;
	public static final String TAG_GCAL_GUESTS_CAN_MODIFY = NS_GCAL+":"+TAG_GUESTS_CAN_MODIFY;
	public static final String TAG_GCAL_GUESTS_CAN_SEE_GUESTS = NS_GCAL+":"+TAG_GUESTS_CAN_SEE_GUESTS;
	public static final String TAG_GCAL_SEQUENCE = NS_GCAL+":"+TAG_SEQUENCE;
	public static final String TAG_GCAL_UID = NS_GCAL+":"+TAG_UID;
	public static final String TAG_GD_RECURRENCE = NS_GD+":"+TAG_RECURRENCE;

	//アトリビュート名の定義    
	public static final String ATT_XMLNS = "xmlns";
	public static final String ATT_KIND = "kind";
	public static final String ATT_TERM = "term";
	public static final String ATT_SCHEME = "scheme";
	public static final String ATT_REL = "rel";
	public static final String ATT_HREF = "href";
	public static final String ATT_VALUE = "value";
	public static final String ATT_VALUE_STRING = "valueString";
	public static final String ATT_EMAIL = "email";
	public static final String ATT_ENDTIME = "endTime";
	public static final String ATT_STARTTIME = "startTime";
	public static final String ATT_ETAG = "etag";
	public static final String ATT_FEED_LINK = "feedLink";
	public static final String ATT_METHOD = "method";
	public static final String ATT_MINUTES = "minutes";
	public static final String ATT_TYPE = "type";

	//NameSpace付きアトリビュート名の定義
	public static final String ATT_GD_ETAG = NS_GD+":"+ATT_ETAG;
	public static final String ATT_GD_KIND = NS_GD+":"+ATT_KIND;
	public static final String ATT_GD_FEED_LINK = NS_GD+":"+ATT_FEED_LINK;

	//Value名の定義
	public static final String VAL_NEXT = "next";
	public static final String VAL_EDIT = "edit";
	public static final String VAL_SELF = "self";
	public static final String VAL_TEXT = "text";
	public static final String VAL_ALTERNATE = "alternate";
	public static final String VAL_ALERT = "alert";
	//呼び出し元Activity(MainCalendar)のコンテキスト
	private Context contextObj;
	//スケジュールデータベースヘルパーオブジェクト
	private ScheduleDatabaseHelper sdhDB;
	
	/**
	 * コンストラクタ
	 */
	public GDataCalendarParser(Context contextObj, ScheduleDatabaseHelper sdhDB){
		Log.d("DEBUG", "GDataCalendarParser Constractor Start");
		this.contextObj = contextObj;
		this.sdhDB = sdhDB;
		Log.d("DEBUG", "GDataCalendarParser Constractor End");
	}

	/**
	 * パーサ本体
	 *
	 * @param isObj XMLファイルのInputStream
	 * @param lgAlarmFlag TDCC固有端末アラーム機能初期値
	 * @return feedタグに含まれるNextURL 最後のfeedの場合はnull
	 */
	public ArrayList<String> parse(InputStream isObj, long lgAlarmFlag){
		Log.d("DEBUG", "GDataCalendarParser parse Start");
		if(isObj == null){
			return null;
		} else {
			//注意）デバックが必要な場合のみログ出力すること。(InputStreamのカーソルは戻せない為、ログ出しするとパース処理が行えなくなる)
			//Log.d("DEBUG", "GDataCalendarParser parse Target：" + InputCheckUtil.convertStreamToString(isObj));
		}
		//alReturn：1つ目はstrNextUrl,2つ目はエラー発生フラグ
		ArrayList<String> alReturn = new ArrayList<String>();
		alReturn.add(0, null);
		alReturn.add(1, null);
		String strNextUrl = null;
		String strTagName = null;
		String strParent = null;
		GDataEvent gdeObj = null;
		//TAGの階層を保持しておくためのStackの定義
		Stack<String> stkObj = new Stack<String>();
		try {
			//PullParserの初期化
			XmlPullParser xppObj = XmlPullParserFactory.newInstance().newPullParser();
			xppObj.setInput(isObj,null);
			//現在のEventTypeの取得
			int eventType = xppObj.getEventType();
			//Log.d("DEBUG", "GDataCalendarParser parse eventType：" + eventType);
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				//START_DOCUMENTの処理
				case XmlPullParser.START_DOCUMENT:
					//Log.d("DEBUG", "GDataCalendarParser parse Check(1)：" + XmlPullParser.START_DOCUMENT);
					break;
				//START_TAGの処理
				case XmlPullParser.START_TAG:
					//Log.d("DEBUG", "GDataCalendarParser parse Check(2)：" + XmlPullParser.START_TAG);
					//現在のTAGを取得
					strTagName = xppObj.getName();
					if(stkObj.empty() == false){
						//TAGスタックから一つ上位のTAGを取得
						strParent = stkObj.peek();
					}
					//現在のTAGをStackにPush
					stkObj.push(strTagName);
					if(strTagName.equalsIgnoreCase(TAG_ENTRY)){
						//entryタグの処理
						//新しいGDataEventを作成
						gdeObj = new GDataEvent();
						//entry tagのアトリビュートの処理
						for(int intCt = 0; intCt < xppObj.getAttributeCount(); intCt++){
							if(xppObj.getAttributeName(intCt).equalsIgnoreCase(ATT_GD_ETAG)){
								gdeObj.setStrEtag(xppObj.getAttributeValue(intCt));
							}
						}
					}else if(strTagName.equalsIgnoreCase(TAG_LINK)){
						//linkタグの処理
						if(strParent.equalsIgnoreCase(TAG_ENTRY)){
							if(xppObj.getAttributeCount() > 0){
								if(xppObj.getAttributeName(0).equalsIgnoreCase(ATT_REL) 
										&& xppObj.getAttributeValue(0).equalsIgnoreCase(VAL_EDIT)
										&& xppObj.getAttributeName(2).equalsIgnoreCase(ATT_HREF)){
									//最初のアトリビュート名がrelで値がedit　かつ　3番目のat理ビューと名がhrefである場合
									//editURLなのでGDataEventに記録する
									gdeObj.setStrEditUrl(xppObj.getAttributeValue(2));
								}
							}
						}else if(strParent.equalsIgnoreCase(TAG_FEED)){
							//feedタグの処理
							if(xppObj.getAttributeCount() > 0){
								if(xppObj.getAttributeName(0).equalsIgnoreCase(ATT_REL) 
										&& xppObj.getAttributeValue(0).equalsIgnoreCase(VAL_NEXT)){
									// 最初のアトリビュート名がrelで値がnextならば、続きのfeedを取得するためのURLなので、strNextUrlとして保存
									strNextUrl = xppObj.getAttributeValue(2);
								}
							}
						}
					}else if(strTagName.equalsIgnoreCase(TAG_CATEGORY)){
						// categoryタグの処理
						if(strParent.equalsIgnoreCase(TAG_ENTRY)){
							for(int intCt = 0; intCt < xppObj.getAttributeCount(); intCt++){
								if(xppObj.getAttributeName(intCt).equalsIgnoreCase(ATT_TERM)){
									gdeObj.setStrCategory(xppObj.getAttributeValue(intCt));
								}
							}
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_WHERE)){
						// gd_whereタグの処理
						if(xppObj.getAttributeCount() > 0){
							gdeObj.setStrWhere(xppObj.getAttributeValue(0));
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_WHEN)){
						// gd_whenタグの処理
						for(int intCt = 0; intCt < xppObj.getAttributeCount(); intCt++){
							if(xppObj.getAttributeName(intCt).equalsIgnoreCase(ATT_ENDTIME)){
								// アトリビュート名がgd_when_endtimeの場合、イベントの終了時刻
								gdeObj.setGcEnd(xppObj.getAttributeValue(intCt));
							}else if(xppObj.getAttributeName(intCt).equalsIgnoreCase(ATT_STARTTIME)){
								// アトリビュート名がgd_when_starttimeの場合、イベントの開始時刻
								gdeObj.setGcStart(xppObj.getAttributeValue(intCt));
							}
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_REMINDER)){
						if(xppObj.getAttributeCount() == 2){
							gdeObj.addToAlarmMap(xppObj.getAttributeValue(0),xppObj.getAttributeValue(1));
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_EVENT_STATUS)){
						// event_statusタグの処理
						if(xppObj.getAttributeCount() > 0){
							gdeObj.setStrEventStatus(xppObj.getAttributeValue(0));
						}
					}
					break;
				case XmlPullParser.TEXT:
					//Log.d("DEBUG", "GDataCalendarParser parse Check(3)：" + XmlPullParser.TEXT);
					//TAGのCONTET部分の処理
					if(gdeObj != null){
						if(strTagName.equalsIgnoreCase(TAG_PUBLISHED)){
							//publishedタグの処理
							gdeObj.setGcPublished(xppObj.getText());
						}else if(strTagName.equalsIgnoreCase(TAG_UPDATED)){
							//updatedタグの処理
							gdeObj.setGcUpdated(xppObj.getText());
						}else if(strTagName.equalsIgnoreCase(TAG_TITLE)){
							//titleタグの処理
							gdeObj.setStrTitle(xppObj.getText());
						}else if(strTagName.equalsIgnoreCase(TAG_CONTENT)){
							//contentタグの処理
							gdeObj.setStrContent(xppObj.getText());
						}else if(strTagName.equalsIgnoreCase(TAG_ID)){
							//idタグの処理
							gdeObj.setStrEventId(xppObj.getText());
						}else if(strTagName.equalsIgnoreCase(TAG_GD_RECURRENCE)){
							//recurrenceタグの処理
							gdeObj.setStrRecurrence(xppObj.getText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					//Log.d("DEBUG", "GDataCalendarParser parse Check(4)：" + XmlPullParser.END_TAG);
					//タグクローズの処理
					strTagName = xppObj.getName();
					//TAG Stack からPOP
					stkObj.pop();
					if(strTagName.equalsIgnoreCase(TAG_ENTRY)){
						//entryタグの終了の場合は、これまでに記録したgdeObjをデータベースに保存
						//Log.d("DEBUG", "GDataCalendarParser parse Check(update)");
						//端末アラーム対象フラグセット
						gdeObj.setLgAlarmFlag(lgAlarmFlag);
						updateScheduleDB(gdeObj);
						gdeObj = null;
					}
					break;
				}
				// 次のeventに移動
				eventType = xppObj.next();
				//Log.d("DEBUG", "GDataCalendarParser parse Check(5)：" + eventType);
			}
			alReturn.set(0, strNextUrl);
		} catch (SQLException e) {
			Log.e("ERROR", "GDataCalendarParser parse ERROR：", e);
			alReturn.set(1, "ERROR");
		} catch (XmlPullParserException e) {
			Log.e("ERROR", "GDataCalendarParser parse ERROR：", e);
			alReturn.set(1, "ERROR");
		} catch (IOException e) {
			Log.e("ERROR", "GDataCalendarParser parse ERROR：", e);
			alReturn.set(1, "ERROR");
		} finally {
			//Stackオブジェクトの初期化(OutOfMemoryエラー対策)
			stkObj = null;
		}
		Log.d("DEBUG", "GDataCalendarParser parse End");
		return alReturn;
	}

	/**
	 * GDataEventのメンバ変数に基づいてデータベースを更新する
	 * @param gdeObj GData
	 */
	private void updateScheduleDB(GDataEvent gdeObj) throws SQLException{
		Log.d("DEBUG", "GDataCalendarParser updateScheduleDB Start");
		//Recurrenceが存在するデータについてはこのアプリでは無視する
		if(gdeObj.getStrRecurrence() != null){
			Log.d("DEBUG", "GDataCalendarParser updateScheduleDB (Recurrence) End");
			return;
		}
		//タイトルと更新日時を取得する
		Log.d("DEBUG","GDataCalendarParser updateScheduleDB" + gdeObj.getStrTitle() + " : " + gdeObj.getStartTimeString());
		Schedule scheduleObj = new Schedule();
		String[] strArraySelectArgs = {gdeObj.getStrEventId()};
		try {
			//読み込まれたデータのID情報をもとにデータベースを検索
			scheduleObj = selectScheduleUpdated(strArraySelectArgs);
		} catch(SQLException e) {
			//DBReadエラー発生時
			Log.e("Error", "GDataCalendarParser updateScheduleDB selectScheduleUpdated:SQLException", e);
			throw new SQLException(e.toString());
		}
		if(!(scheduleObj.getStrUpdated() == null || scheduleObj.getStrUpdated().equals(""))){
			//すでに存在しているデータの場合は更新処理
			Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：すでに存在しているデータの場合");
			String strDBUpdated = scheduleObj.getStrUpdated();
			GregorianCalendar gcObj = DateUtil.toCalendar(strDBUpdated);
			//更新日時を比較する(Calendar#compareTo)
			//等しければ、0
			//Googleカレンダーの更新日時(mUpdated) が新しい場合は 1
			//データベースの更新日時(gcObj)が新しい場合は -1
			int intComp = gdeObj.getGcUpdated().compareTo(gcObj);
			if(intComp == 0){
				//アップデートの日時が等しい
				//何もしない
				Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：更新日時が等しい");
			} else if(intComp > 0) {
				//Googleカレンダー側が新しいので更新
				Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダー側が新しい");
				if(gdeObj.isCanceled()){
					//Googleカレンダーで削除されたものの場合、データベースから削除
					Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダーで削除されたもの");
					Schedule scObj = new Schedule();
					scObj.setStrCalendarId(gdeObj.getStrEventId());
					boolean blDeleteResult = sdhDB.deleteScheduleCalendarId(scObj);
					if(blDeleteResult == false) {
						//DBWriteエラー発生時
						Log.e("Error", "GDataCalendarParser updateScheduleDB deleteSchedule Error");
						throw new SQLException("DeleteError");
					}
				} else {
					//Googleカレンダーで更新されたものを取り込む
					Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダーで更新されたもの");
					//スケジュール更新処理(Full)
					boolean blsaveScheduleResult = saveScheduleFull(1, gdeObj);
					if(blsaveScheduleResult == false) {
						//DBWriteエラー発生時
						Log.e("Error", "GDataCalendarParser updateScheduleDB saveScheduleFull Error");
						throw new SQLException("UpdateError");
					}
				}
			} else if(intComp < 0) {
				//Googleカレンダー側が古い場合
				Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダー側が古い");
				if((gdeObj.isCanceled() == true) && (scheduleObj.getLgDeleteFlag() == 1)){
					//Googleカレンダー側が削除データで端末データも削除ステータスの場合、どちらが新しくても端末データを削除
					//(削除されたデータを対象に削除通信が発生するとシステムエラーになる)
					Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダー側で削除されたデータの為、データベースから削除");
					Schedule scObj = new Schedule();
					scObj.setStrCalendarId(gdeObj.getStrEventId());
					boolean blDeleteResult = sdhDB.deleteScheduleCalendarId(scObj);
					if(blDeleteResult == false) {
						//DBWriteエラー発生時
						Log.e("Error", "GDataCalendarParser updateScheduleDB deleteSchedule Error");
						throw new SQLException("DeleteError");
					}
				} else {
					//端末側が新しければ後でGoogleカレンダーにアップロードするためにrecordにMODIFIEDフラグをセット
					Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダー側で有効なデータの為、更新フラグセット");
					//スケジュール更新処理
					boolean blupdateScheduleResult = updateScheduleModified(gdeObj);
					if(blupdateScheduleResult == false) {
						//DBWriteエラー発生時
						Log.e("Error", "GDataCalendarParser updateScheduleDB updateScheduleModified Error");
						throw new SQLException("UpdateError");
					}
				}
			}
		}else{
			//DBに同じデータが見つからなかったとき
			Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：端末に同じデータが見つからない場合");
			if(gdeObj.isCanceled()){
				//Googleカレンダー側で削除されていれば何もしない
				Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：カレンダー側が削除されている為、未処理");
				return;
			}
			//Googleカレンダー側の新規登録データ
			Log.d("DEBUG", "GDataCalendarParser updateScheduleDB：Googleカレンダー側の新規作成データを登録");
			boolean blsaveScheduleResult = saveScheduleFull(0, gdeObj);
			if(blsaveScheduleResult == false) {
				//DBWriteエラー発生時
				Log.e("Error", "GDataCalendarParser updateScheduleDB saveScheduleFull Error");
				throw new SQLException("InsertError");
			}
		}
		//スケジュールオブジェクトの初期化(OutOfMemoryエラー対策)
		scheduleObj = null;
		Log.d("DEBUG", "GDataCalendarParser updateScheduleDB End");
	}

	/**
	 * Schedule情報のデータベース読み込み処理
	 * @param strAryArgs SQL条件
	 * @return 処理を行った場合はtrue
	 */
	private Schedule selectScheduleUpdated(String[] strAryArgs) throws SQLException {
		Log.d("DEBUG", "GDataCalendarParser selectScheduleUpdated Start");
		Schedule scheduleObj = new Schedule();
		ScheduleDatabaseHelper.ScheduleCursor sdObj = null;
		try {
			//データベース検索処理
			String[] where_args = strAryArgs;
			//カーソルの取得
			sdObj = sdhDB.getScheduleUpdated(where_args);
			//カーソルの取得
			((Activity) contextObj).startManagingCursor(sdObj);
			Log.d("DEBUG", "GDataCalendarParser selectScheduleUpdated ScheduleCursor Count : " + sdObj.getCount());
			for( int intCt=0; intCt<sdObj.getCount(); intCt++){
				//Scheduleデータ格納
				//DELETED
				scheduleObj.setLgDeleteFlag(sdObj.getColDeleted());
				//TITLE
				scheduleObj.setStrTitle(sdObj.getColTitle());
				//UPDATED
				scheduleObj.setStrUpdated(sdObj.getColUpdated());
				sdObj.moveToNext();
			}
			sdObj.close();
		} catch (SQLException e) {
			Log.e("ERROR", "GDataCalendarParser selectScheduleUpdated DB Error",e);
			if(sdObj != null) {
				sdObj.close();
			}
			throw new SQLException(e.toString());
		} finally {
			if(sdObj != null) {
				sdObj.close();
			}
		}
		Log.d("DEBUG", "GDataCalendarParser selectScheduleUpdated End");
		return scheduleObj;
	}

	/**
	 * Schedule情報のデータベース登録・更新処理
	 *
	 * @param intType 0:insert,1:update
	 * @param gdeObj GData
	 * @return 処理を行った場合はtrue
	 */
	private boolean saveScheduleFull(int intType, GDataEvent gdeObj) {
		Log.d("DEBUG", "GDataCalendarParser saveScheduleFull Start");
		boolean blResult = true;
		//SQLパラメータ作成(ID,DELETED,MODIFIED,ALARM,ALARM_LIST,TITLE,CONTENT,
		//                 GD_WHERE,GD_WHEN_ENDTIME,GD_WHEN_STARTTIME,PUBLISHED,
		//                 UPDATED,CATEGORY,EDIT_URL,GD_EVENTSTATUS,CALENDAR_ID,
		//                 ETAG,ENDTIME,STARTTIME,ALARM_FLAG)
		Schedule objSchedule = new Schedule();
		//DELETED
		objSchedule.setLgDeleteFlag(gdeObj.getLgDeleted());
		//MODIFIED
		objSchedule.setLgModified(gdeObj.getLgModified());
		//TITLE
		objSchedule.setStrTitle(InputCheckUtil.convNullKara(gdeObj.getStrTitle()));
		//GD_WHERE
		objSchedule.setStrGDWhere(InputCheckUtil.convNullKara(gdeObj.getStrWhere()));
		//GD_WHEN_STARTTIME
		objSchedule.setStrGDWhenStartTime(InputCheckUtil.convNullKara(gdeObj.getStartString()));
		//GD_WHEN_ENDTIME
		objSchedule.setStrGDWhenEndTime(InputCheckUtil.convNullKara(gdeObj.getEndString()));
		//CONTENT
		objSchedule.setStrContent(InputCheckUtil.convNullKara(gdeObj.getStrContent()));
		//PUBLISHED
		objSchedule.setStrPublished(InputCheckUtil.convNullKara(gdeObj.getPublishedString()));
		//UPDATED
		objSchedule.setStrUpdated(InputCheckUtil.convNullKara(gdeObj.getUpdatedString()));
		//CATEGORY
		objSchedule.setStrCategory(InputCheckUtil.convNullKara(gdeObj.getStrCategory()));
		//EDIT_URL
		objSchedule.setStrEditUrl(InputCheckUtil.convNullKara(gdeObj.getStrEditUrl()));
		//GD_EVENTSTATUS
		objSchedule.setStrGDEventStatus(InputCheckUtil.convNullKara(gdeObj.getStrEventStatus()));
		//CALENDAR_ID
		objSchedule.setStrCalendarId(InputCheckUtil.convNullKara(gdeObj.getStrEventId()));
		//ETAG
		objSchedule.setStrEtag(InputCheckUtil.convNullKara(gdeObj.getStrEtag()));
		//ALARM(未使用項目の為、ZERO)
		objSchedule.setLgAlarm(0l);
		//ALARM_LIST
		objSchedule.setStrAlarmList(InputCheckUtil.convNullKara(gdeObj.getAlarmMapString()));
		//STARTTIME
		StringBuffer sbStartObj = new StringBuffer();
		sbStartObj.append(gdeObj.getStartDateString());
		sbStartObj.append(" ");
		sbStartObj.append(gdeObj.getStartTimeString());
		objSchedule.setLgStartTime(DateUtil.convMSec(sbStartObj.toString()));
		//Log.d("DEBUG", "GDataCalendarParser saveScheduleFull sbStartObj：" + sbStartObj.toString());
		//Log.d("DEBUG", "GDataCalendarParser saveScheduleFull STARTTIME：" + objSchedule.getLgStartTime());
		//ENDTIME
		StringBuffer sbEndObj = new StringBuffer();
		sbEndObj.append(gdeObj.getEndDateString());
		sbEndObj.append(" ");
		sbEndObj.append(gdeObj.getEndTimeString());
		objSchedule.setLgEndTime(DateUtil.convMSec(sbEndObj.toString()));
		//Log.d("DEBUG", "GDataCalendarParser saveScheduleFull sbEndObj：" + sbEndObj.toString());
		//Log.d("DEBUG", "GDataCalendarParser saveScheduleFull ENDTIME：" + objSchedule.getLgEndTime());
		if(intType == 0) {
			//Insert処理の場合
			//ALARM_FLAG
			objSchedule.setLgAlarmFlag(gdeObj.getLgAlarmFlag());
			blResult = sdhDB.insertScheduleEventIdFull(objSchedule);
		} else {
			//Update処理の場合
			//ALARM_FLAG(TDCCオリジナル項目のため更新不要)
			blResult = sdhDB.updateScheduleEventIdFull(objSchedule);
		}
		//スケジュールオブジェクトの初期化(OutOfMemoryエラー対策)
		objSchedule = null;
		Log.d("DEBUG", "GDataCalendarParser saveScheduleFull End");
		return blResult;
	}

	/**
	 * Schedule情報のデータベース更新処理
	 * 
	 * @param gdeObj GData
	 * @return 処理を行った場合はtrue
	 */
	private boolean updateScheduleModified(GDataEvent gdeObj) {
		Log.d("DEBUG", "GDataCalendarParser updateScheduleModified Start");
		boolean blResult = true;
		//SQLパラメータ作成(MODIFIED,CALENDAR_ID)
		Schedule objSchedule = new Schedule();
		//MODIFIED(SQLite→GoogleCalendar更新予定フラグ)
		objSchedule.setLgModified(1l);
		//UPDATED
		objSchedule.setStrUpdated(DateUtil.toDBDateString(new GregorianCalendar()));
		//CALENDAR_ID
		objSchedule.setStrCalendarId(gdeObj.getStrEventId());
		blResult = sdhDB.updateScheduleChangeModified(objSchedule);
		//スケジュールオブジェクトの初期化(OutOfMemoryエラー対策)
		objSchedule = null;
		Log.d("DEBUG", "GDataCalendarParser updateScheduleModified End");
		return blResult;
	}

	/**
	 * GDataEventの情報を元にinsert用のXMLを作成する。
	 *
	 * @param gdeObj 新規登録すべき内容を持ったGDataEvent
	 * @return String XML
	 */
	public String insertSerializer(GDataEvent gdeObj){
		Log.d("DEBUG", "GDataCalendarParser insertSerializer Start");
		//新規登録用XMLのSerializer
		XmlSerializer xsObj = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			//出力を設定
			xsObj.setOutput(writer);
			//XML Documentを開始
			xsObj.startDocument("UTF-8",false);
			xsObj.startTag("", TAG_ENTRY);
			xsObj.attribute("", NS_XMLNS, "http://www.w3.org/2005/Atom");
			xsObj.attribute("", NS_XMLNS+":"+NS_GD, "http://schemas.google.com/g/2005");
			xsObj.startTag("", TAG_CATEGORY);
			xsObj.attribute("", ATT_SCHEME, "http://schemas.google.com/g/2005#kind");
			xsObj.attribute("", ATT_TERM, "http://schemas.google.com/g/2005#event");
			xsObj.endTag("", TAG_CATEGORY);
			xsObj.startTag("",TAG_TITLE);
			xsObj.attribute("", ATT_TYPE, VAL_TEXT);
			xsObj.text(gdeObj.getStrTitle());
			xsObj.endTag("", TAG_TITLE);
			xsObj.startTag("", TAG_CONTENT);
			xsObj.attribute("", ATT_TYPE, VAL_TEXT);
			xsObj.text(gdeObj.getStrContent());
			xsObj.endTag("", TAG_CONTENT);
			xsObj.startTag("", TAG_GD_TRANSPARENCY);
			xsObj.attribute("", ATT_VALUE, "http://schemas.google.com/g/2005#event.opaque");
			xsObj.endTag("", TAG_GD_TRANSPARENCY);
			xsObj.startTag("", TAG_GD_EVENT_STATUS);
			xsObj.attribute("", ATT_VALUE, "http://schemas.google.com/g/2005#event.confirmed");
			xsObj.endTag("", TAG_GD_EVENT_STATUS);
			xsObj.startTag("", TAG_GD_WHERE);
			xsObj.attribute("", ATT_VALUE_STRING, gdeObj.getStrWhere());
			xsObj.endTag("", TAG_GD_WHERE);
			xsObj.startTag("", TAG_GD_WHEN);
			xsObj.attribute("", ATT_ENDTIME, gdeObj.getEndString());
			xsObj.attribute("", ATT_STARTTIME, gdeObj.getStartString());
			xsObj.endTag("", TAG_GD_WHEN);
			xsObj.endTag("", TAG_ENTRY);
			//Documentを終了
			xsObj.endDocument();
			Log.d("DEBUG", "GDataCalendarParser insertSerializer(1) End：" + writer.toString());
			//結果のXMLをStringで返す
			return writer.toString();
		} catch (Exception ex) {
			Log.e("ERROR", "GDataCalendarParser insertSerializer ERROR：", ex);
		}
		Log.d("DEBUG", "GDataCalendarParser insertSerializer(2) End");
		return null;
	}

	/**
	 * 更新用のXMLを生成する
	 *
	 * @param isObj 更新元のXMLを読み出すInputStream
	 * @param gdeObj 更新すべき内容を持ったGDataEvent
	 * @return String 内容を書き換えたXML
	 */
	public String updateSerializer(InputStream isObj, GDataEvent gdeObj){
		Log.d("DEBUG", "GDataCalendarParser updateSerializer Start");
		//更新用XMLのSerializer
		XmlSerializer xsObj = Xml.newSerializer();
		StringWriter swObj = new StringWriter();
		//更新元を解釈するパーサー
		XmlPullParser xppObj;
		try {
			//初期化
			xppObj = XmlPullParserFactory.newInstance().newPullParser();
			xppObj.setInput(isObj,null);
			xsObj.setOutput(swObj);
			String strTagName = null;
			Stack<String> stkObj = new Stack<String>();
			int eventType = xppObj.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					//Document開始
					xsObj.startDocument("UTF-8",false);
					break;
				case XmlPullParser.START_TAG:
					strTagName = xppObj.getName();
					//TAG開始
					xsObj.startTag("", strTagName);
					stkObj.push(strTagName);
					if(strTagName.equalsIgnoreCase(TAG_ENTRY)){
						for(int i=0;i<xppObj.getAttributeCount();i++){
							if(xppObj.getAttributeName(i).equalsIgnoreCase(ATT_GD_ETAG)){
								xsObj.attribute("",xppObj.getAttributeName(i), xppObj.getAttributeValue(i));
							}else{
								xsObj.attribute("",xppObj.getAttributeName(i), xppObj.getAttributeValue(i));
							}
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_EVENT_STATUS)){
						xsObj.attribute("", ATT_VALUE, "http://schemas.google.com/g/2005#event.confirmed");
					}else if(strTagName.equalsIgnoreCase(TAG_GD_WHERE)){
						if(gdeObj.getStrWhere() != null){
							xsObj.attribute("", ATT_VALUE_STRING, gdeObj.getStrWhere());
						}
					}else if(strTagName.equalsIgnoreCase(TAG_GD_WHEN)){
						for(int i=0;i<xppObj.getAttributeCount();i++){
							if(xppObj.getAttributeName(i).equalsIgnoreCase(ATT_ENDTIME)){
								xsObj.attribute("", ATT_ENDTIME, gdeObj.getEndString());
							}else if(xppObj.getAttributeName(i).equalsIgnoreCase(ATT_STARTTIME)){
								xsObj.attribute("", ATT_STARTTIME, gdeObj.getStartString());
							}
						}
					}else{
						for(int i=0;i<xppObj.getAttributeCount();i++){
							xsObj.attribute("",xppObj.getAttributeName(i), xppObj.getAttributeValue(i));
						}
					}
					break;
				case XmlPullParser.TEXT:
					//TEXTでは、GDataEventに値を持っているものについては処理しない
					//END_TAGの所でTEXTの出力処理を行う。
					if(strTagName.equalsIgnoreCase(TAG_ID)){
					}else if(strTagName.equalsIgnoreCase(TAG_PUBLISHED)){
					}else if(strTagName.equalsIgnoreCase(TAG_UPDATED)){
					}else if(strTagName.equalsIgnoreCase(TAG_TITLE)){
					}else if(strTagName.equalsIgnoreCase(TAG_CONTENT)){
					}else{
						xsObj.text(xppObj.getText());
					}
					break;
				case XmlPullParser.END_TAG:
					//END_TAG処理
					strTagName = xppObj.getName();
					//GDataEventで値を持っている場合はここでtext出力を行う
					if(strTagName.equalsIgnoreCase(TAG_ID)){
						xsObj.text(gdeObj.getStrEventId());
					}else if(strTagName.equalsIgnoreCase(TAG_PUBLISHED)){
						xsObj.text(gdeObj.getPublishedString());
					}else if(strTagName.equalsIgnoreCase(TAG_UPDATED)){
						xsObj.text(gdeObj.getUpdatedString());
					}else if(strTagName.equalsIgnoreCase(TAG_TITLE)){
						xsObj.text(gdeObj.getStrTitle());
					}else if(strTagName.equalsIgnoreCase(TAG_CONTENT)){
						xsObj.text(gdeObj.getStrContent());
					}
					stkObj.pop();
					xsObj.endTag("", strTagName);
					break;
				}
				eventType = xppObj.next();
			}
			xsObj.endDocument();
			Log.d("DEBUG", "GDataCalendarParser updateSerializer(1) End：" + swObj.toString());
			return swObj.toString();
		} catch (Exception e) {
			Log.e("ERROR", "GDataCalendarParser updateSerializer ERROR：", e);
		}
		Log.d("DEBUG", "GDataCalendarParser updateSerializer(2) End");
		return null;
	}
}