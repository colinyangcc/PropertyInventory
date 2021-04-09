package com.example.agc_inventory.entity;

public class BookInfo {
    private String BookNo,BookName,Header,RoomName;

    public BookInfo(String bookNo, String bookName, String header, String roomName) {
        BookNo = bookNo;
        BookName = bookName;
        Header = header;
        RoomName = roomName;
    }

    public String getBookNo() { return BookNo; }
    public void setBookNo(String bookNo) { BookNo = bookNo; }

    public String getBookName() { return BookName; }
    public void setBookName(String bookName) { BookName = bookName; }

    public String getHeader() { return Header; }
    public void setHeader(String header) { Header = header; }

    public String getRoomName() { return RoomName; }
    public void setRoomName(String header) { RoomName = header; }

    @Override
    public String toString() {
        return "BookInfo{" +
                "BookNo='" + BookNo + '\'' +
                ", BookName='" + BookName + '\'' +
                ", Header='" + Header + '\'' +
                ", RoomName='" + RoomName + '\'' +
                '}';
    }
}
