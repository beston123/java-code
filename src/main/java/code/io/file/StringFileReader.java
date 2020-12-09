package code.io.file;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈文件Reader〉<p>
 * 〈大文件读取，支持亿级〉
 *
 * @author zixiao
 * @date 19/1/10
 */
public class StringFileReader implements Closeable{

    private BufferedRandomAccessFile reader;

    private long nextPos;

    public StringFileReader(String filePath) throws IOException {
        this(new File(filePath));
    }

    public StringFileReader(File file) throws IOException {
        this.reader = new BufferedRandomAccessFile(file, "r");
        this.nextPos = 0;
    }

    /**
     * 读取标题
     *
     * @return 标题数据
     */
    public List<String> readHeader() {
        return readLine(0, 1);
    }

    /**
     * 读取数据
     *
     * @param limit 读取行数
     * @return      数据列表
     */
    public List<String> readData(int limit) {
        return readLine(nextPos, limit);
    }

    /**
     * 使用BufferedRandomAccessFile读取文件
     *
     * @param pos    偏移量，从0开始
     * @param limit  读取行数
     * @return  数据列表
     */
    public List<String> readLine(long pos, int limit) {
        List<String> dataList = new ArrayList();
        try {
            reader.seek(pos);
            for (int i = 0; i < limit; i++) {
                String pin = reader.readLine();
                if (pin == null || pin.isEmpty()) {
                    break;
                }
                dataList.add(new String(pin.getBytes("ISO-8859-1"), "UTF-8"));
            }
            nextPos = reader.getFilePointer();
            return dataList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("文件解析异常", e);
        }
    }

    public void close(){
        closeQuietly(reader);
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

}
