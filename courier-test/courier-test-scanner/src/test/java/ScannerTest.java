import io.kenji.courier.common.scanner.ClassScanner;
import io.kenji.courier.common.scanner.consumer.RpcConsumerScanner;
import io.kenji.courier.common.scanner.provider.RpcProviderScanner;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/10
 **/
public class ScannerTest {

    @Test
    public void testScannerClassNameList() throws IOException {
        List<String> classNameList = ClassScanner.getClassNameList("io.kenji.courier.test.scanner");
        classNameList.forEach(System.out::println);
    }

    @Test
    public void testScannerClassNameListByRpcProvider() throws IOException {
        RpcProviderScanner.doScannerWithRpcProviderAnnotationFilterAndRegistryService("io.kenji.courier.test.scanner");
    }

    @Test
    public void testScannerClassNameListByRpcConsumer() throws IOException {
        RpcConsumerScanner.doScannerWithRpcConsumerAnnotationFilter("io.kenji.courier.test.scanner");
    }

}
