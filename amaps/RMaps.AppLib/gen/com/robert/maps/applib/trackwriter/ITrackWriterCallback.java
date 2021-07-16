/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android\\map\\gradle\\RMaps_source_code\\robertprojects-read-only\\RMaps.AppLib\\src\\com\\robert\\maps\\applib\\trackwriter\\ITrackWriterCallback.aidl
 */
package com.robert.maps.applib.trackwriter;
public interface ITrackWriterCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.robert.maps.applib.trackwriter.ITrackWriterCallback
{
private static final java.lang.String DESCRIPTOR = "com.robert.maps.applib.trackwriter.ITrackWriterCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.robert.maps.applib.trackwriter.ITrackWriterCallback interface,
 * generating a proxy if needed.
 */
public static com.robert.maps.applib.trackwriter.ITrackWriterCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.robert.maps.applib.trackwriter.ITrackWriterCallback))) {
return ((com.robert.maps.applib.trackwriter.ITrackWriterCallback)iin);
}
return new com.robert.maps.applib.trackwriter.ITrackWriterCallback.Stub.Proxy(obj);
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
case TRANSACTION_newPointWrited:
{
data.enforceInterface(DESCRIPTOR);
double _arg0;
_arg0 = data.readDouble();
double _arg1;
_arg1 = data.readDouble();
this.newPointWrited(_arg0, _arg1);
return true;
}
case TRANSACTION_onTrackStatUpdate:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
double _arg1;
_arg1 = data.readDouble();
long _arg2;
_arg2 = data.readLong();
double _arg3;
_arg3 = data.readDouble();
double _arg4;
_arg4 = data.readDouble();
long _arg5;
_arg5 = data.readLong();
double _arg6;
_arg6 = data.readDouble();
this.onTrackStatUpdate(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.robert.maps.applib.trackwriter.ITrackWriterCallback
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
@Override public void newPointWrited(double lat, double lon) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeDouble(lat);
_data.writeDouble(lon);
mRemote.transact(Stub.TRANSACTION_newPointWrited, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void onTrackStatUpdate(int Cnt, double Distance, long Duration, double MaxSpeed, double AvgSpeed, long MoveTime, double AvgMoveSpeed) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(Cnt);
_data.writeDouble(Distance);
_data.writeLong(Duration);
_data.writeDouble(MaxSpeed);
_data.writeDouble(AvgSpeed);
_data.writeLong(MoveTime);
_data.writeDouble(AvgMoveSpeed);
mRemote.transact(Stub.TRANSACTION_onTrackStatUpdate, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_newPointWrited = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onTrackStatUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Called when the service has a new value for you.
     */
public void newPointWrited(double lat, double lon) throws android.os.RemoteException;
public void onTrackStatUpdate(int Cnt, double Distance, long Duration, double MaxSpeed, double AvgSpeed, long MoveTime, double AvgMoveSpeed) throws android.os.RemoteException;
}
