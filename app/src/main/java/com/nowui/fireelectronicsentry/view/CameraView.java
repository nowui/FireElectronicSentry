package com.nowui.fireelectronicsentry.view;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 摄像的View
 * @author haozi
 *
 */
public class CameraView extends SurfaceView {

	public Context mContext;
	private SurfaceHolder mSurfaceHolder;
	
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}
	
	public CameraView(Context context) {
		super(context);
		this.mContext = context;
	}
	
	

	/**
	 * 把照相机对象传入
	 * @param mCamera
	 */
	public void setCamera(Camera mCamera){
		
		// 操作surface的holder
		mSurfaceHolder = this.getHolder();
		// 创建surfaceholder对象
		mSurfaceHolder.addCallback(new SurfaceHolderCallback(mCamera));
		// 设置push缓冲类型，说明surface数据由其他来源提供。而不是用自己的Canvas来绘图，在这里由摄像头来提供数据。
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	/**
	 * 得到最适合的预览大小
	 * @param sizes
	 * @param w
	 * @param h
	 * @return
	 */
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
	
	
	/**
	 * 得到最合适的PictureSize
	 * @param sizes
	 * @param w
	 * @param h
	 * @return
	 */
	public static Size getOptimalPictureSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
	
	/**
	 * 摄像头捕捉到的画面都会在这里被处理
	 * @author haozi
	 *
	 */
	class SurfaceHolderCallback implements SurfaceHolder.Callback{

		private Camera mCamera;
		
		public SurfaceHolderCallback(Camera mCamera){
			
			this.mCamera = mCamera;
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			// 停止预览
			mCamera.stopPreview();
			// 释放相机资源并置空
			mCamera.release();
			mCamera = null;
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// 设置预览
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// 如果出现异常，释放相机资源并置空
				mCamera.release();
				mCamera = null;
			}
		}
		
		// 当surface视图数据发生变化时，处理预览信息
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			// 如果相机资源并不为空
			if(mCamera != null){
				
				// 获得相机参数对象
				Camera.Parameters parameters = mCamera.getParameters();
				// 获取最合适的参数，为了做到拍摄的时候所见即所得，我让previewSize和pictureSize相等
				Size previewSize = getOptimalPictureSize(parameters.getSupportedPictureSizes(), 640, 480);
				Size pictureSize = getOptimalPictureSize(parameters.getSupportedPictureSizes(), 640, 480);
				System.out.println("---------------------------------------------------------------");
				System.out.println("previewSize: " + previewSize.width + ", " + previewSize.height);
				System.out.println("pictureSize: " + pictureSize.width + ", " + pictureSize.height);
				// 设置照片格式
				parameters.setPictureFormat(PixelFormat.JPEG);
				// 设置预览大小
				parameters.setPreviewSize(previewSize.width, previewSize.height);
				// 设置自动对焦，先进行判断
				List<String> focusModes = parameters.getSupportedFocusModes();  
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {  
					parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				}	
				// 设置图片保存时候的分辨率大小
				parameters.setPictureSize(pictureSize.width, pictureSize.height);
				// 给相机对象设置刚才设置的参数
				mCamera.setParameters(parameters);
				// 开始预览
				mCamera.startPreview();
			}
		}
	}
}
