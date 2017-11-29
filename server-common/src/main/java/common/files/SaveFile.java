package common.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SaveFile<T> implements Serializable {

	private static final long serialVersionUID = 8063721852444447972L;
	public static final String SAVE_FOLDER = "server-data";

	private File file;

	public SaveFile(String rm, String key) {
		this(rm, key, "data");
	}

	public SaveFile(String rm, String key, String extension) {
		file = new File(SAVE_FOLDER + "/" + rm + "/" + key + "." + extension);
	}

	public void save(T data) throws IOException {
		// Create the file if it does not exist
		file.getParentFile().mkdirs();
		file.createNewFile();

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(data);

		oos.close();
	}

	@SuppressWarnings("unchecked")
	public T read() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);

		T obj = (T) ois.readObject();
		ois.close();

		return obj;
	}
}
