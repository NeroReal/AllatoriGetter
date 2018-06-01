import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

public enum Allatori {

	INSTANCE;

	private File directory = new File(".");
	private String url = "http://www.allatori.com/";

	public void start(String[] args) {
		try {
			@SuppressWarnings("unused")
			String dev = "Nero";

			System.out.println("Checking for update...");
			String content = getContent(url);
			String downloadurl = "";
			String version = "";

			Scanner scanner = new Scanner(content);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("Download") && line.contains("bottommenulink")) {
					String replace = line.replace("\">Download</a> <span class=\"bottommenulinkdivider\">|</span>", "")
							.replace("<a class=\"bottommenulink\" href=\"", "");

					String replace2 = line
							.replace("-Demo.zip\">Download</a> <span class=\"bottommenulinkdivider\">|</span>", "")
							.replace("<a class=\"bottommenulink\" href=\"downloads/Allatori-", "");

					version = replace2;
					downloadurl = url + replace;
				}
			}

			System.err.println("Found Download at " + downloadurl);

			scanner.close();

			File file = new File(directory + "/lib/Allatori-" + version + ".zip");

			System.out.println("Downloading...");
			FileUtils.copyURLToFile(new URL(downloadurl), file);

			String filename = unzip(file, new File(directory + "/lib/"));

			FileUtils.forceDelete(file);

			FileUtils.copyFileToDirectory(new File(filename + "lib/allatori.jar"), new File(directory + "/lib/"));

			FileUtils.copyFileToDirectory(new File(filename + "lib/allatori-annotations.jar"),
					new File(directory + "/lib/"));

			FileUtils.deleteDirectory(new File(filename));

			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getContent(String nurl) {
		String pageText = null;
		try {
			URL url = new URL(nurl);
			URLConnection conn = url.openConnection();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				pageText = reader.lines().collect(Collectors.joining("\n"));
			}
		} catch (Exception e) {
			pageText = null;
		}

		return pageText;
	}

	private String unzip(File zipFilePath, File destDirectory) {
		try {
			if (!destDirectory.exists()) {
				destDirectory.mkdir();
			}
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
			ZipEntry entry = zipIn.getNextEntry();
			String filename = destDirectory + File.separator + entry.getName();
			while (entry != null) {
				String filePath = destDirectory + File.separator + entry.getName();

				if (!entry.isDirectory()) {
					if (filePath.contains("lib/")) {
						extractFile(zipIn, filePath);
					}
				} else {
					File dir = new File(filePath);
					dir.mkdir();
				}

				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}

			zipIn.close();
			return filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void extractFile(ZipInputStream zipIn, String filePath) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[1024];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
