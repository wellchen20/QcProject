/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android\\map\\gradle\\RMaps_source_code\\robertprojects-read-only\\RMaps.AppLib\\src\\com\\robert\\maps\\applib\\downloader\\IDownloaderCallback.aidl
 */
package com.robert.maps.applib.downloader;
public interface IDownloaderCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.robert.maps.applib.downloader.IDownloaderCallback
{
private static final java.lang.String DESCRIPTOR = "com.robert.maps.applib.downloader.IDownloaderCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.robert.maps.applib.downloader.IDownloaderCallback interface,
 * generating a proxy if needed.
 */
public static com.robert.maps.applib.downloader.IDownloaderCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.robert.maps.applib.downloader.IDownloaderCallback))) {
return ((com.robert.maps.applib.downloader.IDownloaderCallback)iin);
}
return new com.robert.maps.applib.downloader.IDownloaderCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_downloadDone:
{
data.enforceInterface(DESCRIPTOR);
this.downloadDone();
return true;
}
case TRANSACTION_downloadStart:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
int _arg4;
_arg4 = data.readInt();
int _arg5;
_arg5 = data.readInt();
int _arg6;
_arg6 = data.readInt();
int _arg7;
_arg7 = data.readInt();
int _arg8;
_arg8 = data.readInt();
this.downloadStart(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8);
return true;
}
case TRANSACTION_downloadTileDone:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _arg4;
_arg4 = data.readInt();
this.downloadTileDone(_arg0, _arg1, _arg2, _arg3, _arg4);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.robert.maps.applib.downloader.IDownloaderCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Called when the service has a new value for you.
     */
@Override public void downloadDone() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_downloadDone, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void downloadStart(int tileCnt, long startTime, java.lang.String fileName, java.lang.String mapID, int zoom, int lat0, int lon0, int lat1, int lon1) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(tileCnt);
_data.writeLong(startTime);
_data.writeString(fileName);
_data.writeString(mapID);
_data.writeInt(zoom);
_data.writeInt(lat0);
_data.writeInt(lon0);
_data.writeInt(lat1);
_data.writeInt(lon1);
mRemote.transact(Stub.TRANSACTION_downloadStart, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void downloadTileDone(int tileCnt, int errorCnt, int x, int y, int z) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(tileCnt);
_data.writeInt(errorCnt);
_data.writeInt(x);
_data.writeInt(y);
_data.writeInt(z);
mRemote.transact(Stub.TRANSACTION_downloadTileDone, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_downloadDone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_downloadStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_downloadTileDone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Called when the service has a new value for you.
     */
public void downloadDone() throws android.os.RemoteException;
public void downloadStart(int tileCnt, long startTime, java.lang.String fileName, java.lang.String mapID, int zoom, int lat0, int lon0, int lat1, int lon1) throws android.os.RemoteException;
public void downloadTileDone(int tileCnt, int errorCnt, int x, int y, int z) throws android.os.RemoteException;
}
