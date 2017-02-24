package com.linkai.app.libraries;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.linkai.app.R;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by LP1001 on 18-07-2016.
 */
public class FileHandler {
    private String TAG="FileHandler";
    Context context;
    Common common;
    DatabaseHandler db;
    Resources res;
    RequestQueue requestQueue;

    Bitmap up_bitmap;
    String up_file_name;

    final String IMAGE_EXT="jpg";


    public FileHandler(Context _context){
        this.context=_context;
//        init db
        db=Const.DB;
//        init common
        common=new Common(context);
        res=context.getResources();
//        creating directores if not created
        common.createAppDirectories();
//        initializing volley requestqueue
        requestQueue= Volley.newRequestQueue(context);
    }



//function for file upload.
    public int uploadFile(final String filepath ){
//        for progress bar
        int total;
        int sent_count=0;
        float progress=0;
//
        //String fileName=msg.getFileName();

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
//        String filepath=Environment.getExternalStorageDirectory() + File.separator +res.getString(R.string.dir_sent)+File.separator+fileName;

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024;
        int maxBufferSize = 32 * 1024 ;
        File selectedFile = new File(filepath);
        Log.d(TAG, "uploadFile: filepath--"+filepath);
        if (!selectedFile.isFile()){
//            Log.d(TAG, "uploadFile: not file"+filepath);
        }else{

            try{
//                Log.d(TAG, "uploadFile: in try");
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(Const.UPLOAD_FILE_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",filepath);
                connection.setRequestProperty("file_name",filepath.substring(filepath.lastIndexOf('/')+1));
                Log.d(TAG, "uploadFile: file_name "+filepath.substring(filepath.lastIndexOf('/')+1));
                //creating new dataoutputstream

                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + filepath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                total=bytesAvailable;
                //selecting the buffer size as minimum of available bytes or 32kb
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
//                Log.d(TAG, "uploadFile: sending");
                while (bytesRead > 0){
                    //      write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    //                    calculate progress
                    sent_count+=bytesRead;
                    progress=(sent_count*100)/total;
                    Log.d(TAG, "uploadFile:progress- "+progress);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
//                if(serverResponseCode == 200){
////                    //                        update message
////                    msg.setFileStatus(1);
////                    db.updateMessage(msg);
////                    //Log.d("uploading file", "uploadFile: success");
//                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: FileNotFoundException-"+e.getMessage());
            } catch (MalformedURLException e) {
                Log.d(TAG, "uploadFile: MalformedURLException-"+e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: IOException-"+e.getMessage());
            }
            catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "uploadFile: Exception-"+e.getMessage());
            }

        }
        return serverResponseCode;
    }

//    function to upload profile image
    public int uploadProfileImage(final String filepath ,int profileType){
    //        for progress bar
        int total;
        int sent_count=0;
        float progress=0;
    //
        //String fileName=msg.getFileName();

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
    //        int maxBufferSize = 1 * 1024 * 1024;
        int maxBufferSize = 32 * 1024 ;
        File selectedFile = new File(filepath);
    //        Log.d(TAG, "uploadFile: filepath--"+filepath);
        if (!selectedFile.isFile() || !selectedFile.exists()){
            Log.d(TAG, "uploadFile: not file"+filepath);
        }else{

            try{
    //                Log.d(TAG, "uploadFile: in try");
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(Const.UPLOAD_PROFILE_IMAGE_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",filepath);
                connection.setRequestProperty("JabberId",db.getUser().getJabberId());
                connection.setRequestProperty("Password",db.getUser().getPassword());
                connection.setRequestProperty("Ext","jpg");
                connection.setRequestProperty("profileType",String.valueOf(profileType));
                connection.setRequestProperty("filename",filepath.substring(filepath.lastIndexOf('/')+1));

                //creating new dataoutputstream

                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + filepath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                total=bytesAvailable;
                //selecting the buffer size as minimum of available bytes or 32kb
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
    //                Log.d(TAG, "uploadFile: sending");
                while (bytesRead > 0){
                    //      write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                    //                    calculate progress
                    sent_count+=bytesRead;
                    progress=(sent_count*100)/total;
                    Log.d(TAG, "uploadFile:progress- "+progress);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode+"-");
                if(serverResponseCode!=200) {
                    InputStream inStr=connection.getErrorStream();
                    StringBuilder sbError=new StringBuilder();
                    BufferedReader br=new BufferedReader(new InputStreamReader(inStr));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sbError.append(line); // + "\r\n"(no need, json has no line breaks!)
                    }
                    br.close();
                    Log.d(TAG, "uploadProfileImage: Error- "+sbError.toString());
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: FileNotFoundException-"+e.getMessage());
            } catch (MalformedURLException e) {
                Log.d(TAG, "uploadFile: MalformedURLException-"+e.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: IOException-"+e.getMessage());
            }
            catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "uploadFile: Exception-"+e.getMessage());
            }

        }
        return serverResponseCode;
    }


//    2 overloading functions for downloading file. one for single chat and another for groupchat
//  SingleChat/ChatMessage:  to download. only called in a service or thread
    public boolean downloadFile(ChatMessage message){
//        ChatMessage message=db.getMessageById(msg_id);
        String outFilePath;
        if(message.getType().equals(ChatMessage.TYPE_IMAGE)){
            outFilePath=res.getString(R.string.dir_images);
        }
        else if(message.getType().equals(ChatMessage.TYPE_VIDEO)){
            outFilePath=res.getString(R.string.dir_video);
        }
        else if(message.getType().equals(ChatMessage.TYPE_AUDIO)){
            outFilePath=res.getString(R.string.dir_audio);
        }
        else if(message.getType().equals(ChatMessage.TYPE_OTHERS)){
            outFilePath=res.getString(R.string.dir_others);
        }
        else{
            return false;
        }

        String out_file_name=Environment.getExternalStorageDirectory() + File.separator+outFilePath + File.separator + message.getFileName();;


        String fileUrl=Const.FILE_DOWNLOAD_URL+File.separator+message.getFileName();

        int count;
        try {
            URL url = new URL(fileUrl);
            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lengthOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(out_file_name);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress(""+(int)((total*100)/lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

//            updating message in db
            message.setFileStatus(1);
            db.updateMessage(message);
            //Log.d("filehandler", "downloadFile: ");
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            return true;

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return false;
        }
    }

    //  Group Chat/GroupMessage:  to download. only called in a service or thread
    public boolean downloadFile(GroupMessage message){
//        ChatMessage message=db.getMessageById(msg_id);
        String outFilePath;
        if(message.getType().equals(ChatMessage.TYPE_IMAGE)){
            outFilePath=res.getString(R.string.dir_images);
        }
        else if(message.getType().equals(ChatMessage.TYPE_VIDEO)){
            outFilePath=res.getString(R.string.dir_video);
        }
        else if(message.getType().equals(ChatMessage.TYPE_AUDIO)){
            outFilePath=res.getString(R.string.dir_audio);
        }
        else if(message.getType().equals(ChatMessage.TYPE_OTHERS)){
            outFilePath=res.getString(R.string.dir_others);
        }
        else{
            return false;
        }

        String out_file_name=Environment.getExternalStorageDirectory() + File.separator+outFilePath + File.separator + message.getFileName();;


        String fileUrl=Const.FILE_DOWNLOAD_URL+File.separator+message.getFileName();

        int count;
        try {
            URL url = new URL(fileUrl);
            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            OutputStream output = new FileOutputStream(out_file_name);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress(""+(int)((total*100)/lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

//            updating message in db
            message.setFileStatus(1);
            db.updateGroupMessage(message);
            //Log.d("filehandler", "downloadFile: ");
            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
            return true;

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            return false;
        }

    }

    public boolean copyFile(File src,File dst)  {
        if(!dst.exists()){
            try {
                dst.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inChannel != null)
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            if (outChannel != null)
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

//    resize and copy image
    public boolean copyAndResizeImage(String inImg,String outImg){
//        file output stream to save bitmap to file
        try {
            FileOutputStream out = null;
            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
//        float maxHeight = 2000.0f;
//        float maxWidth = 2000.0f;
            Bitmap outBitmap = compressImage(maxWidth, maxHeight, inImg);
            if (outBitmap == null) {
                return false;
            }
//        init out filestream
            try {
                out = new FileOutputStream(outImg);
                outBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

//        releasing objects
            finally {
                if (out == null) {
                    try {
                        out.close();
                        outBitmap.recycle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//    to save image
    public boolean saveImage(String path,Bitmap bitmap){
        boolean ret=false;
        FileOutputStream outFile=null;
        try {
            outFile=new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
            ret=true;
        } catch (Exception e) {
            e.printStackTrace();
            ret=false;
        }
        finally {
            if(outFile==null){
                try {
                    outFile.close();
                    bitmap.recycle();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public boolean deleteFile(String path){
        File file=new File(path);
        if(file.exists()){
            return file.delete();
        }
        else {
            return false;
        }
    }

//    to get thumb nail of image
    public String getThumbImage(String filename){
        String fileName= Environment.getExternalStorageDirectory() + File.separator +res.getString(R.string.dir_sent)+File.separator+filename;
        Bitmap outBitmap=null;
        try {
            outBitmap = compressImage(64.0f, 64.0f, fileName);
            if (outBitmap != null) {
                return bitmapToString(outBitmap);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String getThumbOfVideo(String path,boolean largeThumb){
        Bitmap thumb=null;
        try {
            if(largeThumb) {
                thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            else{
                thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MICRO_KIND);
            }
        }catch (Exception e){
            e.printStackTrace();

        }

        return bitmapToString(thumb);
    }

//    string to generate file name
    public String generateFileName(String ext){
        String filename=""+System.currentTimeMillis()+"."+ext;
        return filename;
    }

//    convert bitmap to string
    public  String bitmapToString(Bitmap bmp){
        if(bmp==null){return null;}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

//    converting string to bitmap
    public  Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

//    function to get bitmap of an image by its full file path
    public Bitmap getBitmap(String full_file_path){
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(full_file_path);
            return bitmap;
        }catch (Exception e){
            //Log.d("filehandler", "getBitmap: exception-"+e.getMessage());
            return null;
        }
    }

//    2 overloading functions "getFilePath" .. one passes ChatMessage object and other GroupMessage Object
    // ChatMessage:   function to get directory of a file with respect to message object type
    public String getFilePath(ChatMessage msg){
        String full_file_path=Environment.getExternalStorageDirectory() + File.separator;
        if(msg.getType()==ChatMessage.TYPE_TEXT){
            full_file_path="";
            return "";
        }
        else if(msg.getFrom().equals("self")){
            full_file_path+=res.getString(R.string.dir_sent)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_IMAGE)){
            full_file_path+=res.getString(R.string.dir_images)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_VIDEO)){
            full_file_path+=res.getString(R.string.dir_video)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_AUDIO)){
            full_file_path+=res.getString(R.string.dir_audio)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_OTHERS)){
            full_file_path+=res.getString(R.string.dir_others)+File.separator;
        }
        full_file_path+=msg.getFileName();
        //Log.d("Filehandler", "getFilePath: "+full_file_path);
        return full_file_path;
    }

    // GroupMessage:   function to get directory of a file with respect to message object type
    public String getFilePath(GroupMessage msg){
        String full_file_path=Environment.getExternalStorageDirectory() + File.separator;
        if(msg.getType()==ChatMessage.TYPE_TEXT){
            full_file_path="";
            return "";
        }
        else if(msg.getFrom().equals("self")){
            full_file_path+=res.getString(R.string.dir_sent)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_IMAGE)){
            full_file_path+=res.getString(R.string.dir_images)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_VIDEO)){
            full_file_path+=res.getString(R.string.dir_video)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_AUDIO)){
            full_file_path+=res.getString(R.string.dir_audio)+File.separator;
        }
        else if(msg.getType().equals(ChatMessage.TYPE_OTHERS)){
            full_file_path+=res.getString(R.string.dir_others)+File.separator;
        }
        full_file_path+=msg.getFileName();
        //Log.d("Filehandler", "getFilePath: "+full_file_path);
        return full_file_path;
    }

//    function to open a file
    public void openFile(String filepath){
        File file = new File(filepath);
        Uri file_uri=Uri.fromFile(file);
        ContentResolver cr=context.getContentResolver();
        String extension= MimeTypeMap.getFileExtensionFromUrl(filepath);
        String mime_type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Log.d("Filehandler", "openFile: file-"+filepath+" mime-"+mime_type);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(file_uri,mime_type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try{
            context.startActivity(intent);
        }catch (Exception e){
            Toast.makeText(context,"Cannot open file. Try again",Toast.LENGTH_SHORT).show();
        }

    }

//    create random file name
    public String createRandomFileName(){
       return db.getUser().getJabberId().replace("+","")+"_"+(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
    }

//    compress image with specified width and height and return bitmap
    public Bitmap compressImage(float maxWidth,float maxHeight,String inImgName){
        Bitmap outBitmap=null;
        Bitmap inBitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        inBitmap= BitmapFactory.decodeFile(inImgName,options);
//        getting bitmap size
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;


        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
//        Log.d("Filehandler", "compressImage: "+imgRatio+"-"+maxRatio);
//        Log.d("Filehandler", "compressImage: "+actualHeight+"-"+actualWidth);
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
//                Log.d("Filehandler", "compressImage: 1=1");
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }


//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            inBitmap = BitmapFactory.decodeFile(inImgName, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            return null;
        }
        try {
            outBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            return null;
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(outBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(inBitmap, middleX - inBitmap.getWidth() / 2, middleY - inBitmap.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(inImgName);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            //Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                //Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                //Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                //Log.d("EXIF", "Exif: " + orientation);
            }
            outBitmap = Bitmap.createBitmap(outBitmap, 0, 0,
                    outBitmap.getWidth(), outBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outBitmap;

    }

//    The method calculateInSampleSize:: calculates a proper value for inSampleSize based on the actual and required dimensions:
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

}
