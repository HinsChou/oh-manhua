package com.manhua.oh

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // 加密
    @Throws(Exception::class)
    fun Encrypt(sSrc: String, sKey: String?): String? {
        if (sKey == null) {
            print("Key为空null")
            return null
        }
        // 判断Key是否为16位
        if (sKey.length != 16) {
            print("Key长度不是16位")
            return null
        }
        val raw = sKey.toByteArray(charset("utf-8"))
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher =
            Cipher.getInstance("AES/ECB/PKCS5Padding") //"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        val encrypted = cipher.doFinal(sSrc.toByteArray(charset("utf-8")))
        return Base64.getEncoder().encodeToString(encrypted) //此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    @Throws(Exception::class)
    fun Decrypt(sSrc: String?, sKey: String?): String? {
        return try {
            // 判断Key是否正确
            if (sKey == null) {
                print("Key为空null")
                return null
            }
            // 判断Key是否为16位
            if (sKey.length != 16) {
                print("Key长度不是16位")
                return null
            }
            val raw = sKey.toByteArray(charset("utf-8"))
            val skeySpec =
                SecretKeySpec(raw, "AES")
            val cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            // 两次解码
            val data = Base64.getDecoder().decode(sSrc)
            val encrypted1 = Base64.getDecoder().decode(data)

            try {
                val original = cipher.doFinal(encrypted1)
                String(original, Charset.forName("utf-8"))
            } catch (e: Exception) {
                println(e.toString())
                null
            }
        } catch (ex: Exception) {
            println(ex.toString())
            null
        }
    }

    @Test
    @Throws(Exception::class)
    fun main() {
        /*
         * 此处使用AES-128-ECB加密模式，key需要为16位。
         */
        val cKey = "JRUIFMVJDIWE569j"
//        // 需要加密的字串
//        val cSrc = "mh_info={imgpath:\"12902/预告/\",startimg:1,totalimg:12,mhid:\"12902\",mhname:\"妹妹别盘我！\",pageid:910650,pagename:\"预告\",pageurl:\"1/1.html\",readmode:3,maxpreload:5,defaultminline:1,domain:\"img.ohmanhua.com\",manga_size:\"\",default_price:0,price:0};image_info={img_type:\"\",urls__direct:\"\",line_id:1,local_watch_url:\"\"}"
//        println(cSrc)
//        // 加密
//        val enString: String? = Encrypt(cSrc, cKey)
//        println("加密后的字串是：$enString")

        var data =
            "b3VhbEEwbGU5VnBzellzaGZySXhOdjVDYjhrM1hvTnZCSG02cDA4cytDbElOSnBmUForV29tdkh0T1dZbmprK1pEM1dSU05XOFM2aDJ3Si9SNkdLa0xhRnlwbXJSekNVb1FVMlpnSEpHejBJdWp3Y1RlZVVNcFZ5bkNELzhHYnN2NlpPUFI3ZUJHcWNpcmh2bDJwU1A4NkJIeGRudEI0Y3VuRmxTV1g3OUFVaGtMeXZQYlN0SXRicGoxYndBNWMvYlp3SG1yZlZWV2Nqak4rT2JhYTBPOEdzNzlYeTFVTHkzcW4xa3NQRWFRUFhxOG55Rm0za2ZRWmRHQkREbzIwZHZ6cExyd240L0dEQlU3dEFKVzM0YzNIa1g0ZGxveDRldTdNV2s5d1UxVnJnU3RRN25rR2YvUUhrc3JhNTRwZElhODI4ZzBzWktPTFcrRHNnUFkwYVBaK1VnRW5sVFVETHcySlRpRmExK3RFaWZXemVhZjMzTGYydFZnNy9Va0dXUEkxZEFaMWI3Ly9lQnhZTkVWcWY4T1BPcTlibFNPSUFlSkFlRGd1L04ybDloSVM2Q1pLeFZUSHo5Wkk1ZXBCbDVBeWs5NkF1NlF2UkM0QSttS1VETkMyLzBPS2VBK3EydnRQTG9oWXY5TUkycDM5dHhtUGNQNDBqVzFTOS9jNHQ="

        // 解密
        val DeString: String? = Decrypt(data, cKey)
        println("解密后的字串是：$DeString")
    }


}
