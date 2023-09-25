package com.fran.aab;

import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import brut.androlib.Config;
import brut.common.BrutException;
import brut.util.AaptManager;
import brut.util.Jar;

/**
 * @author 程良明
 * @date 2023/9/8
 * * * 说明:从Apktool解包路径下生成aab
 * 参考https://juejin.cn/post/6982111395621896229
 **/

public class Apk2Aab {
	private final String mApkDecodePath;
	private final String mWorkPath;

	public static void main(String[] args) throws IOException {

		Apk2Aab aab = new Apk2Aab("D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf");
		aab.process();
	}

	/**
	 * 构造方法
	 *
	 * @param apkDecodePath apktool解包后的文件路径
	 */
	public Apk2Aab(String apkDecodePath) {
		mApkDecodePath = apkDecodePath;
		mWorkPath = Utils.linkPath(apkDecodePath, "fran_base_work");
		File workFile = new File(mWorkPath);
		if (workFile.exists()) {
//			Utils.delDir(workFile);
		}
		workFile.mkdirs();
	}

	public void process() throws IOException {
//		String compileFIlePath = compile();
//		String baseApkPath = linkSources(compileFIlePath);
//		String basePath = unZipBase(baseApkPath);
//		copySources(basePath);
		copySources("D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf\\fran_base_work\\base");
	}


	private String getAapt2Path() {
		try {
			File aapt = AaptManager.getAapt2();
			return aapt.getPath();
		} catch (BrutException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 编译资源
	 */
	private String compile() {
		String compileFIlePath = Utils.linkPath(mWorkPath, "compiled_resources.zip");
		String aaptPath = getAapt2Path();
		String cmdCompile = String.format("%s compile --legacy --dir %s -o %s", aaptPath, Utils.linkPath(mApkDecodePath, "res"), compileFIlePath);
		try {
			RuntimeHelper.getInstance().run(cmdCompile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compileFIlePath;
	}

	/**
	 * 关联资源
	 */
	private String linkSources(String compileFIlePath) {
		String outBaseApk = Utils.linkPath(mWorkPath, "base.apk");
		String aaptPath = getAapt2Path();
		// TODO: 2023/9/25 读取apktool的yaml获取
		String androidJarPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\android.jar";
		String minVersion = "24";
		String targetVersion = "31";
		String versionCode = "1";
		String versionName = "1.0.0";

		String cmdLink = String.format("%s link --proto-format -o %s -I %s --min-sdk-version %s --target-sdk-version %s --version-code %s --version-name %s --manifest %s -R %s --auto-add-overlay",
						aaptPath, outBaseApk, androidJarPath, minVersion, targetVersion, versionCode, versionName, Utils.linkPath(mApkDecodePath, "AndroidManifest.xml"), compileFIlePath);
		try {
			RuntimeHelper.getInstance().run(cmdLink);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outBaseApk;
	}


	/**
	 * 解压base.apk
	 * 通过unzip解压到base文件夹，目录结构：
	 */
	private String unZipBase(String apkFilePath) {

		String destDirectory = Utils.linkPath(mWorkPath, "base");
		byte[] buffer = new byte[1024];
		try {
			// 创建解压缩输入流
			FileInputStream fis = new FileInputStream(apkFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(destDirectory + File.separator + fileName);
				// 创建目录
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			fis.close();
			System.out.println("解压缩完成");
		} catch (IOException e) {
			e.printStackTrace();
		}


		return destDirectory;
	}

	/**
	 * 拷贝资源
	 */
	private void copySources(String basePath) throws IOException {

		// TODO: 2023/9/25 完成文件copy操作
		String apkDecodeBasePath = basePath;
		String apkDecodePath = mWorkPath;

		File androidManifestFile = new File(Utils.linkPath(apkDecodeBasePath, "AndroidManifest.xml"));
		if (androidManifestFile.exists()) {
			Utils.copyOperation(androidManifestFile, new File(Utils.linkPath(apkDecodeBasePath, "manifest", "AndroidManifest.xml")));
		}

		File assetsFile = new File(Utils.linkPath(apkDecodePath, "assets"));
		if (assetsFile.exists()) {
			Utils.copyOperation(assetsFile, new File(Utils.linkPath(apkDecodeBasePath, "assets")));
		}

		File libFile = new File(Utils.linkPath(apkDecodePath, "lib"));
		if (libFile.exists()) {
			Utils.copyOperation(libFile, new File(Utils.linkPath(apkDecodeBasePath, "lib")));
		}

		File unknownFile = new File(Utils.linkPath(apkDecodePath, "unknown"));
		if (unknownFile.exists()) {
			Utils.copyOperation(unknownFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
		}


		File kotlinFile = new File(Utils.linkPath(apkDecodePath, "kotlin"));
		if (kotlinFile.exists()) {
			Utils.copyOperation(kotlinFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
		}

	}
}
