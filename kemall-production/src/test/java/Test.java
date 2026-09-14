import com.kemall.product.ProductApplication;
import com.kemall.product.domain.cache.SkuCache;
import com.kemall.product.domain.cache.SpuCache;
import com.kemall.product.domain.po.ProductSku;
import com.kemall.product.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = ProductApplication.class)
public class Test {

    @Autowired
    private ProductServiceImpl productService;


    @org.junit.jupiter.api.Test
    public void test(){
        SkuCache sku = productService.getProductSku(1L);
        SpuCache spu = productService.getProductSpu(1L);
        log.info("sku:{}",sku);
        log.info("spu:{}",spu);
    }
}
