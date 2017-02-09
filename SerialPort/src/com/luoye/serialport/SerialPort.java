/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.luoye.serialport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Build;
import android.util.Log;

/**
 * ������
 * 
 * @author LUOYE
 * @data 2015-07-05 11:03:15
 */
public class SerialPort {
	/** Log��־�����ʶ */
	private static final String TAG = "SerialPort";

	/** �����ļ�����������ֹɾ��������������Ϊnative��رմ���ʱ��Ҫʹ�� */
	private FileDescriptor mFd;
	/** �����������ڽ��մ������� */
	private FileInputStream mFileInputStream;
	/** ����������ڷ��ʹ������� */
	private FileOutputStream mFileOutputStream;

	/**
	 * ���캯��
	 * 
	 * @param device
	 *            ������
	 * @param baudrate
	 *            ������
	 * @param flags
	 *            ������ʶ
	 * @throws SecurityException
	 *             ��ȫ�쳣���������ļ����ɶ�дʱ����
	 * @throws IOException
	 *             IO�쳣����������ʧ��ʱ����
	 */
	public SerialPort(File device, int baudrate, int flags)
			throws SecurityException, IOException {
		/* ����豸����Ȩ�ޣ����ļ���Ȩ������ */
		if (!device.canRead() || !device.canWrite()) {
			try {
				if (Build.VERSION.SDK_INT >= 23) {
					String file_contexts = "/device/sprd/scx20/common/sepolicy/file_contexts";
					String device_te = "/device/sprd/scx20/common/sepolicy/device.te";
					String untrusted_app_te = "/device/sprd/scx20/common/sepolicy/untrusted_app.te";

					String text1 = "/dev/abc u:object_r:abc_device:s0";
					String text2 = "type abc_device, dev_type, mlstrustedobject;";
					String text3 = "allow untrusted_app adc_device:chr_fileoperate;";

					String text4 = "echo ";
					String text5 = ">>";

					for (int i = 0; i < 3; i++) {
						String content = "";
						String filePath = "";
						if (i == 0) {
							content = text1;
							filePath = file_contexts;
						} else if (i == 1) {
							content = text2;
							filePath = device_te;
						} else if (i == 2) {
							content = text3;
							filePath = untrusted_app_te;
						}
						Process su;
						su = Runtime.getRuntime().exec("/system/bin/su");
						String cmd = text4 + content + text5 + filePath + "\n";
						su.getOutputStream().write(cmd.getBytes());
						if ((su.waitFor() != 0)) {
							throw new SecurityException();
						}
					}
				}

				/* ��û�ж�/дȨ�ޣ�����chmod���豸 */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	/**
	 * ��ȡ������
	 * 
	 * @return ����������
	 */
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	/**
	 * ��ȡ�����
	 * 
	 * @return ���������
	 */
	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	/**
	 * ԭ���������������������ļ�
	 * 
	 * @param path
	 *            ���������ļ�·��
	 * @param baudrate
	 *            ������
	 * @param flags
	 *            ������ʶ
	 * @return
	 */
	private native static FileDescriptor open(String path, int baudrate,
			int flags);

	/**
	 * ԭ���������رմ��������ļ�
	 */
	public native void close();

	static {
		System.loadLibrary("serial_port");
	}
}
