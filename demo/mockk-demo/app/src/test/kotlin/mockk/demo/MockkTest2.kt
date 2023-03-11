package mockk.demo

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.apache.http.HttpHost
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.search.SearchResponse
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.search.SearchHit
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class S3Client {
    private val client = software.amazon.awssdk.services.s3.S3Client.builder()
        .region(Region.AP_NORTHEAST_1)
        .build()

    fun get(bucket: String, key: String): GetObjectResponse {
        val request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        val res = this.client.getObject(request)
        return res.response()
    }
}

internal class MockkTest2 : FunSpec({
    test("test") {
        val mock = mockk<S3Client> {
            every { get(any(), any()) } returns mockk {
                every { versionId() } returns "version1.0"
            }
        }

        mock.get("bucket", "/test/1").versionId() shouldBe "version1.0"
    }
})

class OpenSearchClient {
    private val client = RestHighLevelClient(
        RestClient.builder(HttpHost("localhost", 8080))
    )

    fun search(): SearchResponse {
        val request = SearchRequest()
        return this.client.search(request, RequestOptions.DEFAULT)
    }
}

internal class OpenSearchClientTest : FunSpec({
    test("test") {
        val mock = mockk<OpenSearchClient> {
            every { search() } returns mockk {
                every { hits } returns mockk {
                    every { iterator() } returns mutableListOf<SearchHit>(
                        mockk {
                            every { id } returns "1"
                        },
                        mockk {
                            every { id } returns "2"
                        }
                    ).iterator()
                }
            }
        }

        mock.search().hits.forEach {
            it.id shouldBeIn listOf("1", "2")
        }
    }
})