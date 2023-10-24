package com.scii.util;

import java.io.File;
import java.io.BufferedOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scii.constants.HRMSConstants;

import com.scii.model.Attendance;
import com.scii.model.Employee;
import com.scii.service.EmployeeService;
import com.scii.service.InsertAttendanceService;
import com.scii.model.AttendanceOutgoing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Component
public class CommonUtil {

	@Autowired
	private EmployeeService empService;
	
	@Autowired
	private InsertAttendanceService attendanceService;

	private Logger logger = Logger.getLogger(getClass());

	private static SecretKeySpec secretKey;
	private static byte[] key;
	static String secret = "ssshhhhhhhhhhh!!!!";

	public static String getSecurePassword(String password) throws NoSuchAlgorithmException {
		String generatedPassword = null;

		/* generates the random salt */

		byte[] salt = { -42, -10, 3, 81, -104, 8, -110, -124, 39, -124, 127, -59, 64, 122, 87, 93 };

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt);
		byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (byte b : hashedPassword) {
			sb.append(String.format("%02x", b));
		}
		generatedPassword = sb.toString();
		return generatedPassword;
	}

	// Function to generate random alpha-numeric password of specific length
	public static String generateRandomPassword() {
		// ASCII range - alphanumeric (0-9, a-z)
		final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";

		SecureRandom secureRandom = new SecureRandom();
		StringBuilder stringBuilderPass = new StringBuilder();

		// each iteration of loop choose a character randomly from the given ASCII range
		// and append it to StringBuilder instance

		for (int i = 0; i < 8; i++) {
			int randomIndex = secureRandom.nextInt(chars.length());
			stringBuilderPass.append(chars.charAt(randomIndex));
		}

		return stringBuilderPass.toString();
	}

	/* private Logger logger = Logger.getLogger(getClass()); */

	/**
	 * @brief Function to load configuration files
	 * @param Configuration file name to load
	 * @return Object of Properties
	 * @throws IOException
	 */
	public static Properties loadPropertyFile(String propertyFileName) throws IOException {
		File propertyFilePath;
		Properties properties = null;
		FileInputStream fileInputStream = null;
		try {
			propertyFilePath = ResourceUtils.getFile("classpath:" + propertyFileName);
			properties = new Properties();
			fileInputStream = new FileInputStream(propertyFilePath);
			properties.load(fileInputStream);
			return properties;
		} catch (Exception e) {
			/* logger.error(e); */
			return null;
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	/**
	 * method is used to encrypt the user details
	 * 
	 * @param strToEncrypt
	 * @param secret
	 * @return encrypted String
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidKeyException
	 */
	public static String encrypt(String strToEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		setKey(secret);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] cText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
		String finalString = Base64.getEncoder().encodeToString(cText);
		finalString = finalString.replace("+", "-");
		finalString = finalString.replace("/", "_");
		return finalString;
	}

	/**
	 * method is used to decrypt the encrypted user details
	 * 
	 * @param strToDecrypt
	 * @param secret
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public static String decrypt(String strToDecrypt) throws NoSuchAlgorithmException, NoSuchPaddingException,
		InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		strToDecrypt = strToDecrypt.replace("-", "+");
		strToDecrypt = strToDecrypt.replace("_", "/");
		setKey(secret);
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] cText = Base64.getDecoder().decode(strToDecrypt);
		return new String(cipher.doFinal(cText));
	}

	/*
	 * method to get key
	 */
	public static void setKey(String myKey) throws NoSuchAlgorithmException {
		MessageDigest sha = null;
		key = myKey.getBytes(StandardCharsets.UTF_8);
		sha = MessageDigest.getInstance("SHA-1");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		secretKey = new SecretKeySpec(key, "AES");
	}

	/**
	 * @brief Function to current date time(timestamp)
	 */
	public synchronized String getCurrentTimestamp() {
		String dateFormat = "ddMMyyyyHHmmss";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		Date date = new Date();
		String currentDate = formatter.format(date);
		return currentDate;
	}

	/* Display Operating System */
	public String getOperatingSystem() {
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		if (operatingSystem.contains("win")) {
			operatingSystem = "windows";
		} else if (operatingSystem.contains("nix") || operatingSystem.contains("nux")
				|| operatingSystem.contains("aix")) {
			operatingSystem = "linux";
		}
		return operatingSystem;
	}
	
	/* Display System Date*/

    public String getCurrentSystemDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();  
        return dtf.format(now);
    }
    
	/**
	 * @brief method is used to upload file to server
	 * @param File path with file name
	 * @param File to be uploaded
	 * @throws Exception
	 */
	public void uploadFileToServer(String path, MultipartFile file) throws Exception {

		File f = new File(path);
		if (!f.exists()) {
			f.mkdir();
		}
		String filename = FilenameUtils.getName(path + file.getOriginalFilename());

		byte[] bytes = file.getBytes();
		try (BufferedOutputStream stream = new BufferedOutputStream(
				new FileOutputStream(new File(path + File.separator + filename)))) {

			stream.write(bytes);
			stream.flush();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/*
	 * public File convertMultiPartToFile(MultipartFile file, String topicPath) {
	 * Path path = Paths.get(topicPath +
	 * (getOperatingSystem().equalsIgnoreCase("windows") == true ? "\\" : "/") +
	 * file.getOriginalFilename().toString()); File filePath = new
	 * File(path.toString()); return filePath; }
	 */

	/*
	 * @Description this method is used to calculate working_hours based on
	 * punch_in_time or punch_out_time it returns a list
	 */
	public Object calculatePunchIn_PunchOutTime(Attendance attendance) throws ParseException, IOException {
		DateFormat formatTos = new SimpleDateFormat("HH:mm");
		Properties properties = CommonUtil.loadPropertyFile("application.properties");
		String officeStartTime = properties.getProperty("office.engineer.starttime");
		String officeEndTime = properties.getProperty("office.engineer.endtime");

		String officeHouseKeeperStartTime = properties.getProperty("office.outsource.starttime");
		String officeHouseKeeperEndTime = properties.getProperty("office.outsource.endtime");

		String officeGardenerStartTime = properties.getProperty("office.gardener.starttime");
		String officeGardenerEndTime = properties.getProperty("office.gardener.endtime");

		Date startTime = formatTos.parse(officeStartTime); // Office Start Time
		Date endTime = formatTos.parse(officeEndTime); // Office End Time

		Date startHouseKeeperTime = formatTos.parse(officeHouseKeeperStartTime); // Housekeeper Start Time
		Date endHouseKeeperTime = formatTos.parse(officeHouseKeeperEndTime); // HouseKeeper End Time

		Date startGardenerTime = formatTos.parse(officeGardenerStartTime); // Housekeeper Start Time
		Date endGardenerTime = formatTos.parse(officeGardenerEndTime); // HouseKeeper End Time

		String officeLunchTime = properties.getProperty("office.engineers.lunchtime");
		String officeHouseKeeperLunchTime = properties.getProperty("office.outsource.lunchtime");
		String officeGardenerLunchTime = properties.getProperty("office.gardener.lunchtime");

		Date lunchTime = formatTos.parse(officeLunchTime); // Lunch Time
		Date lunchHouseKeeperTime = formatTos.parse(officeHouseKeeperLunchTime); // Lunch Time
		Date lunchGardenerTime = formatTos.parse(officeGardenerLunchTime); // Lunch Time

		long inTimeDifference = 0;
		long punchInTimeDifference = 0; // If any Employee punch in after 08:50

		long outTimeDifference = 0;
		long punchOutTimeDifference = 0; // If any Employee punch out before 18:00
		double lateComing = 0;
		double earlyLeaving = 0;
		String totalWorkingHours = properties.getProperty("employee.engineer.workinghours");
		String totalAlliedJobWorkingHours = properties.getProperty("employee.workers.workinghours");
		
		String halfdayWorkingHours = properties.getProperty("employees.halfday.workinghours");

		List<Attendance> empListAttendance = new ArrayList<>();
		/* for(Attendance attendance : attendanceList) { */
		double totalWorkingHoursEngineers = Double.parseDouble(totalWorkingHours);
		double totalWorkingHoursAlliedjob = Double.parseDouble(totalAlliedJobWorkingHours);
		
		double halfdayEmployeeWorkingHours = Double.parseDouble(halfdayWorkingHours);
		
		Attendance empAttendance = new Attendance();
		Employee employeeModel = new Employee();

		String punchInTime = attendance.getPunch_in_time();
		String punchOutTime = attendance.getPunch_out_time();

		employeeModel.setEmp_id(attendance.getEmp_id());
		employeeModel.setActive_flg("1");

		List empList = empService.authUser(employeeModel);

		if (empList.size() >= 1) {
			employeeModel = (Employee) empList.get(0);
			if (punchInTime!= null
					&& !punchInTime.equals("") 
					&& !punchInTime.isEmpty() 
					&& !punchInTime.equals("undefined")) {
				Date punchInDateTime = formatTos.parse(punchInTime);
				if (!employeeModel.getEmp_type().equalsIgnoreCase(properties.getProperty("employee.type.outsource"))) {
					if (employeeModel.getDesg_name().equalsIgnoreCase(properties.getProperty("employee.desg.facility1"))) {
						if (lunchTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startHouseKeeperTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startHouseKeeperTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 30) {
									lateComing = .5;
									// countOfLateComing = countOfLateComing + lateComing;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 30 && punchInTimeDifference <= 70) {
									lateComing = 1;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 60 && punchInTimeDifference <= 90) {
									lateComing = 1.5;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 90 && punchInTimeDifference <= 120) {
									lateComing = 2;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 120) {
									lateComing = 4;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(endTime) < 0) {
								outTimeDifference = endTime.getTime() - punchOutDateTime.getTime();
								punchOutTimeDifference = outTimeDifference / (60 * 1000);

								if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
									// attendanceModelReq.setEarly_leaving(0.5);
									earlyLeaving = 0.5;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - earlyLeaving;
								} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
									earlyLeaving = 1;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - earlyLeaving;
								} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
									earlyLeaving = 1.5;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - earlyLeaving;
								} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
									earlyLeaving = 2;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - earlyLeaving;
								} else if (punchOutTimeDifference > 120) {
									earlyLeaving = 4;
									totalWorkingHoursAlliedjob = totalWorkingHoursAlliedjob - earlyLeaving;
								}
							}
							attendance.setEarly_leaving(earlyLeaving);
							attendance.setWorking_hours(totalWorkingHoursAlliedjob);
							// empListAttendance.add(attendance);
						}
					} else if (employeeModel.getDesg_name().equalsIgnoreCase(properties.getProperty("employee.desg.facility2"))) {

						if (lunchHouseKeeperTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startGardenerTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startGardenerTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 30) {
									lateComing = 0.5;
									// countOfLateComing = countOfLateComing + lateComing;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 30 && punchInTimeDifference <= 60) {
									lateComing = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 60 && punchInTimeDifference <= 90) {
									lateComing = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 90 && punchInTimeDifference <= 120) {
									lateComing = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 120) {
									lateComing = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(endGardenerTime) < 0) {
								outTimeDifference = endGardenerTime.getTime() - punchOutDateTime.getTime();
								punchOutTimeDifference = outTimeDifference / (60 * 1000);

								if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
									// attendanceModelReq.setEarly_leaving(0.5);
									earlyLeaving = 0.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
									earlyLeaving = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
									earlyLeaving = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
									earlyLeaving = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 120) {
									earlyLeaving = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								}
							}
							attendance.setEarly_leaving(earlyLeaving);
							attendance.setWorking_hours(totalWorkingHoursEngineers);
							// empListAttendance.add(attendance);
						}
					} else {
						if (lunchTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 40) {
									lateComing = .5;
									// countOfLateComing = countOfLateComing + lateComing;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 40 && punchInTimeDifference <= 70) {
									lateComing = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 70 && punchInTimeDifference <= 100) {
									lateComing = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 100 && punchInTimeDifference <= 130) {
									lateComing = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 130) {
									lateComing = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}else {
							totalWorkingHoursEngineers = halfdayEmployeeWorkingHours;
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(lunchTime) < 0) {
									outTimeDifference = lunchTime.getTime() - punchOutDateTime.getTime();
									punchOutTimeDifference = outTimeDifference / (60 * 1000);

									if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
										// attendanceModelReq.setEarly_leaving(0.5);
										earlyLeaving = 0.5;
										halfdayEmployeeWorkingHours = halfdayEmployeeWorkingHours - earlyLeaving;
									} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
										earlyLeaving = 1;
										halfdayEmployeeWorkingHours = halfdayEmployeeWorkingHours - earlyLeaving;
									} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
										earlyLeaving = 1.5;
										halfdayEmployeeWorkingHours = halfdayEmployeeWorkingHours - earlyLeaving;
									} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
										earlyLeaving = 2;
										halfdayEmployeeWorkingHours = halfdayEmployeeWorkingHours - earlyLeaving;
									} else if (punchOutTimeDifference > 120) {
										earlyLeaving = 4;
										halfdayEmployeeWorkingHours = halfdayEmployeeWorkingHours - earlyLeaving;
									}
									attendance.setEarly_leaving(earlyLeaving);
									attendance.setWorking_hours(halfdayEmployeeWorkingHours);
								}else {
								if (punchOutDateTime.compareTo(endTime) < 0) {
									outTimeDifference = endTime.getTime() - punchOutDateTime.getTime();
									punchOutTimeDifference = outTimeDifference / (60 * 1000);

									if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
										// attendanceModelReq.setEarly_leaving(0.5);
										earlyLeaving = 0.5;
										totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
									} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
										earlyLeaving = 1;
										totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
									} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
										earlyLeaving = 1.5;
										totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
									} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
										earlyLeaving = 2;
										totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
									} else if (punchOutTimeDifference > 120) {
										earlyLeaving = 4;
										totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
									}
								}
								attendance.setEarly_leaving(earlyLeaving);
								attendance.setWorking_hours(totalWorkingHoursEngineers);
							}
							
							// empListAttendance.add(attendance);
						}

					}
				} else if (employeeModel.getEmp_type().equalsIgnoreCase(properties.getProperty("employee.type.outsource"))) {
					if (employeeModel.getDesg_name().equalsIgnoreCase(properties.getProperty("employee.desg.housekeeper"))) {
						if (lunchHouseKeeperTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startHouseKeeperTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startHouseKeeperTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 30) {
									lateComing = .5;
									// countOfLateComing = countOfLateComing + lateComing;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 30 && punchInTimeDifference <= 60) {
									lateComing = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 60 && punchInTimeDifference <= 90) {
									lateComing = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 90 && punchInTimeDifference <= 120) {
									lateComing = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 120) {
									lateComing = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}else {
							totalWorkingHoursEngineers = halfdayEmployeeWorkingHours;
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(endTime) < 0) {
								outTimeDifference = endHouseKeeperTime.getTime() - punchOutDateTime.getTime();
								punchOutTimeDifference = outTimeDifference / (60 * 1000);

								if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
									// attendanceModelReq.setEarly_leaving(0.5);
									earlyLeaving = 0.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
									earlyLeaving = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
									earlyLeaving = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
									earlyLeaving = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 120) {
									earlyLeaving = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								}
							}
							attendance.setEarly_leaving(earlyLeaving);
							attendance.setWorking_hours(totalWorkingHoursEngineers);
							// empListAttendance.add(attendance);
						}
					} else if (employeeModel.getDesg_name().equalsIgnoreCase(properties.getProperty("employee.desg.gardener"))) {
						if (lunchGardenerTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startGardenerTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startGardenerTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 30) {
									lateComing = .5;
									// countOfLateComing = countOfLateComing + lateComing;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 30 && punchInTimeDifference <= 60) {
									lateComing = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 60 && punchInTimeDifference <= 90) {
									lateComing = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 90 && punchInTimeDifference <= 120) {
									lateComing = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 120) {
									lateComing = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}else {
							totalWorkingHoursEngineers = halfdayEmployeeWorkingHours;
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(endGardenerTime) < 0) {
								outTimeDifference = endGardenerTime.getTime() - punchOutDateTime.getTime();
								punchOutTimeDifference = outTimeDifference / (60 * 1000);

								if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
									earlyLeaving = 0.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
									earlyLeaving = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
									earlyLeaving = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
									earlyLeaving = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 120) {
									earlyLeaving = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								}
							}
							attendance.setEarly_leaving(earlyLeaving);
							attendance.setWorking_hours(totalWorkingHoursEngineers);
						}
					} else if (employeeModel.getDesg_name().equalsIgnoreCase(properties.getProperty("employee.desg.driver"))) {
						if (lunchTime.compareTo(punchInDateTime) > 0) {
							if (punchInDateTime.compareTo(startTime) > 0) {
								inTimeDifference = punchInDateTime.getTime() - startTime.getTime();
								punchInTimeDifference = inTimeDifference / (60 * 1000);
								if (punchInTimeDifference > 0 && punchInTimeDifference <= 30) {
									lateComing = .5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
								} else if (punchInTimeDifference > 30 && punchInTimeDifference <= 70) {
									lateComing = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 60 && punchInTimeDifference <= 90) {
									lateComing = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 90 && punchInTimeDifference <= 120) {
									lateComing = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								} else if (punchInTimeDifference > 120) {
									lateComing = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - lateComing;
									// empAttendance.setWorking_hours(totalWorkingHours);
								}
							}
							attendance.setLate_coming(lateComing);
						}

						if (!punchOutTime.equals("") && !punchOutTime.isEmpty() && !punchOutTime.equalsIgnoreCase(null)
								&& !punchOutTime.equals("undefined")) {
							Date punchOutDateTime = formatTos.parse(punchOutTime);

							if (punchOutDateTime.compareTo(endTime) < 0) {
								outTimeDifference = endTime.getTime() - punchOutDateTime.getTime();
								punchOutTimeDifference = outTimeDifference / (60 * 1000);

								if (punchOutTimeDifference > 0 && punchOutTimeDifference <= 30) {
									earlyLeaving = 0.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 30 && punchOutTimeDifference <= 60) {
									earlyLeaving = 1;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 60 && punchOutTimeDifference <= 90) {
									earlyLeaving = 1.5;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 90 && punchOutTimeDifference <= 120) {
									earlyLeaving = 2;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								} else if (punchOutTimeDifference > 120) {
									earlyLeaving = 4;
									totalWorkingHoursEngineers = totalWorkingHoursEngineers - earlyLeaving;
								}
							}
							attendance.setEarly_leaving(earlyLeaving);
							attendance.setWorking_hours(totalWorkingHoursEngineers);
						}
					}
				}
			}
			if(attendance.getOut_going_flg() != null && attendance.getOut_going_flg().equalsIgnoreCase("1")) {
				if(attendance.getPunch_out_time() != null
						&& !attendance.getPunch_out_time().isEmpty()
						&& !attendance.getPunch_out_time().equals("")
						&& !attendance.getPunch_out_time().equals("undefined")) {
					AttendanceOutgoing outgoingModel = new AttendanceOutgoing();
					outgoingModel.setEmp_id(attendance.getEmp_id());
					outgoingModel.setPunch_date(attendance.getPunch_date());
				
                    double workingHours = attendance.getWorking_hours();
                    List<AttendanceOutgoing> outgoingFromList = attendanceService.getoutgoingFromList(outgoingModel);
                    List<AttendanceOutgoing> outgoingInList = attendanceService.getoutgoingInList(outgoingModel);  
                    
                    String outgoingFrom;
                    String outgoingIn;
                    double totalOutgoingMinDiff;
                    double totalOutgoingHourDiff;
                    double totalDiff = 0;
                    double totalOutgoingDiff = 0;
                   int j = outgoingFromList.size();
					for(int i=0;i<j;i++) {
						
						AttendanceOutgoing outgoingFromModel = (AttendanceOutgoing) outgoingFromList.get(i);
						AttendanceOutgoing outgoingInModel = (AttendanceOutgoing) outgoingInList.get(i);
						outgoingFrom = outgoingFromModel.getOut_going_from_time();
						outgoingIn = outgoingInModel.getOut_going_in_time();
						String outgoingFromHour = outgoingFrom.substring(0,2);
						String outgoingFromMin = outgoingFrom.substring(3,5);
						if(outgoingFromMin.equals("00")|| outgoingFromMin.equals("30")) {
							int outgoingFromMinInt = Integer.parseInt(outgoingFromMin)+1;
							outgoingFromMin = Integer.toString(outgoingFromMinInt);
						}
						
						String outgoingInHour = outgoingIn.substring(0,2);
						String outgoingInMin = outgoingIn.substring(3,5);
						if(Integer.parseInt(outgoingFromHour) == Integer.parseInt(outgoingInHour)) {
							if(Integer.parseInt(outgoingFromMin) >= 00 && Integer.parseInt(outgoingInMin) <= 30) {
								totalOutgoingDiff = totalOutgoingDiff+0.5;
							}else if(Integer.parseInt(outgoingFromMin) >= 30 && Integer.parseInt(outgoingInMin) <= 59) {
								totalOutgoingDiff = totalOutgoingDiff+0.5;
							}else{
								totalOutgoingDiff = totalOutgoingDiff+1.0;
							}
						}else if(Integer.parseInt(outgoingFromHour) != Integer.parseInt(outgoingInHour)) {
							if(Integer.parseInt(outgoingFromMin) == Integer.parseInt(outgoingInMin)) {
								if(Integer.parseInt(outgoingFromMin) >=01 && Integer.parseInt(outgoingFromMin) <=30) {
									if(Integer.parseInt(outgoingInHour) == 13) {
										totalOutgoingDiff = totalOutgoingDiff + Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
									}else if(Integer.parseInt(outgoingInHour) == 14) {
										totalOutgoingDiff = totalOutgoingDiff + Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) - 0.5;
									}else {
										if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
											totalDiff = totalOutgoingHourDiff - totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}else {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
											totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}
										
									}
								}else if(Integer.parseInt(outgoingFromMin) >=31){
									if(Integer.parseInt(outgoingInHour) == 13) {
										totalOutgoingDiff = totalOutgoingDiff + Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) - 0.5;
									}else if(Integer.parseInt(outgoingInHour) == 14) {
										totalOutgoingDiff = totalOutgoingDiff + Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) - 0.5;
									}else {
										if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
											totalDiff = totalOutgoingHourDiff - totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}else {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
											totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}
									}
									
								}
							}else if(Integer.parseInt(outgoingFromMin) != Integer.parseInt(outgoingInMin)) {
								if((Integer.parseInt(outgoingFromMin) >= 01 && Integer.parseInt(outgoingFromMin) <=30)
										&& (Integer.parseInt(outgoingInMin) >= 01 && Integer.parseInt(outgoingInMin) <=30)) {
									if(Integer.parseInt(outgoingInHour) == 13) {
										totalOutgoingDiff = totalOutgoingDiff + Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour);
									}else {
										if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) - 1;
											totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}else {
											totalOutgoingMinDiff = 0.5;
											totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
											totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
											totalOutgoingDiff = totalOutgoingDiff+totalDiff;
										}										
									}
								}else if((Integer.parseInt(outgoingFromMin) >= 01 && Integer.parseInt(outgoingFromMin) <=30)
										&& (Integer.parseInt(outgoingInMin) >= 31)) {
									if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
										totalOutgoingMinDiff = 1.0;
										totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) - 1;
										totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}else if(Integer.parseInt(outgoingInHour) == 13) {
										totalOutgoingDiff = totalOutgoingDiff+Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
									}else {
										totalOutgoingMinDiff = 1.0;
										totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
										totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}
								}else if((Integer.parseInt(outgoingFromMin) >= 31)
										&& (Integer.parseInt(outgoingInMin) >= 01 && Integer.parseInt(outgoingInMin) <=30)) {
									if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
										if(Integer.parseInt(outgoingInHour) == 14) {
											totalOutgoingDiff = totalOutgoingDiff + Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) -1;
										}else {
											totalOutgoingDiff = totalOutgoingDiff + Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) -1;
										}
									}else {
										if(Integer.parseInt(outgoingInHour) == 13) {
											totalOutgoingDiff = totalOutgoingDiff + Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) - 0.5;
										}else {
											totalOutgoingDiff = totalOutgoingDiff + Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
										}
										
									}
								}else if((Integer.parseInt(outgoingFromMin) >= 31)
										&& (Integer.parseInt(outgoingInMin) >= 31)) {
									if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
										totalDiff = Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) - 0.5;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}else if(Integer.parseInt(outgoingInHour) == 13) {
										totalDiff = Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) - 0.5;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}else{
										totalOutgoingMinDiff = 0.5;
										totalOutgoingHourDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
										totalDiff = totalOutgoingHourDiff + totalOutgoingMinDiff;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}
								}else if((Integer.parseInt(outgoingFromMin) >= 01 && Integer.parseInt(outgoingFromMin) <=30)
										&& (Integer.parseInt(outgoingInMin) == 00)) {
									if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
										totalDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour) -1;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}else {
										totalDiff = Double.parseDouble(outgoingInHour) - Double.parseDouble(outgoingFromHour);
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}
								}else if((Integer.parseInt(outgoingFromMin) >= 31)
										&& (Integer.parseInt(outgoingInMin) == 00)) {
									if(Integer.parseInt(outgoingFromHour) < 13 && Integer.parseInt(outgoingInHour) >= 14) {
										totalDiff = Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour) -1.5;
										totalOutgoingDiff = totalOutgoingDiff+totalDiff;
									}else {
										totalOutgoingDiff = totalOutgoingDiff + Integer.parseInt(outgoingInHour) - Integer.parseInt(outgoingFromHour)-0.5;
									}
								}
								
							}
							
						}
					}
					
					if(totalOutgoingDiff > 0) {
						workingHours = workingHours-totalOutgoingDiff;
					}
					
					if(workingHours >= 4 && workingHours < 6) {
						workingHours = 4;
					}
					
					if(workingHours < 2) {
						workingHours = 0;
					}
					
					attendance.setWorking_hours(workingHours);
                }
			}			
		}
		return attendance;
	}

	public String listAllFiles(String folderPath) throws Exception {
		String validationResult = "invalidExtn";
		File folder = new File(folderPath);
		File[] fileNames = folder.listFiles();
		for (File file : fileNames) {
			if (file.isDirectory()) {
				validationResult = listAllFiles(file.getPath());
			}
		}
		return validationResult;
	}
	
	/**
	 * @throws IOException
	 * @brief Function to move file from source to destination
	 */
	public boolean moveFile(String src, String dest) {
		File srcDir = new File(src);
		File destDir = new File(dest);
		boolean result = srcDir.renameTo(destDir);
		// FileUtils.copyDirectory(srcDir, destDir);
		return result;
	}

	/**
	 * @return
	 * @throws IOException
	 * @brief Function to move file from source to destination
	 */
	public void copyDirectory(String src, String dest) throws IOException {
		File srcDir = new File(src);
		File destDir = new File(dest);

		FileUtils.copyDirectory(srcDir, destDir);

		FileUtils.cleanDirectory(srcDir);
	}

	public String getCellValue(Cell cell) {
		if (cell != null) {
			if (cell.getCellType() == CellType.STRING) {
				return cell.getStringCellValue().trim();
			} else if (cell.getCellType() == CellType.NUMERIC) {
				return String.valueOf((long) cell.getNumericCellValue()).trim();
		    }
		}
		return null;
	}

	public String getDateCellValue(Cell cell) {
		if (cell != null) {
			if (cell.getCellType() == CellType.NUMERIC) {
				Date date = cell.getDateCellValue();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				return dateFormat.format(date);
			} else if (cell.getCellType() == CellType.STRING) {
				return cell.getStringCellValue();
			}
		}
		return null;
	}
}
