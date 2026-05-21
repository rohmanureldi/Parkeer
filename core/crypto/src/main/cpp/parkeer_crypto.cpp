#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/ptrace.h>
#include <android/log.h>

// ─── Anti-Debug ───

static volatile int g_integrity_ok = 0;

__attribute__((constructor))
static void anti_debug_init() {
    if (ptrace(PTRACE_TRACEME, 0, 0, 0) == -1) {
        // Being debugged — poison state
        g_integrity_ok = 0;
        return;
    }
    g_integrity_ok = 1;
}

static int is_frida_present() {
    FILE *f = fopen("/proc/self/maps", "r");
    if (!f) return 0;
    char line[512];
    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, "frida") || strstr(line, "gadget") || strstr(line, "xposed")) {
            fclose(f);
            return 1;
        }
    }
    fclose(f);
    return 0;
}

static int check_integrity() {
    if (!g_integrity_ok) return 0;
    if (is_frida_present()) return 0;
    return 1;
}

// ─── Key Derivation ───
// The master key is derived from multiple obfuscated parts at runtime.
// Never stored as a single contiguous string.

static void derive_master_key(uint8_t *out) {
    // Part A (XOR-masked)
    static const uint8_t part_a[] = {
            0x2B, 0x7E, 0x15, 0x16, 0x28, 0xAE, 0xD2, 0xA6,
            0xAB, 0xF7, 0x15, 0x88, 0x09, 0xCF, 0x4F, 0x3C
    };
    // Part B (XOR-masked)
    static const uint8_t part_b[] = {
            0x3A, 0xD7, 0x7B, 0xB4, 0x0D, 0x7A, 0x36, 0x60,
            0xA8, 0x9E, 0xCA, 0xF3, 0x24, 0x66, 0xEF, 0x97
    };
    // XOR mask applied at runtime
    static const uint8_t mask[] = {
            0x5A, 0x1C, 0x3E, 0x72, 0x8F, 0xD4, 0xE1, 0xC6,
            0x03, 0x69, 0xDF, 0x9B, 0x2D, 0xA9, 0xA0, 0xAB
    };

    for (int i = 0; i < 16; i++) {
        out[i] = part_a[i] ^ mask[i];
        out[16 + i] = part_b[i] ^ mask[i];
    }
}

// ─── HMAC-SHA256 (simplified for key derivation) ───
// Uses Android's JNI callback to Java's Mac for actual HMAC

static jbyteArray do_hmac_sha256(JNIEnv *env, jbyteArray key, jbyteArray data) {
    jclass macClass = (*env)->FindClass(env, "javax/crypto/Mac");
    jmethodID getInstance = (*env)->GetStaticMethodID(env, macClass, "getInstance",
                                                      "(Ljava/lang/String;)Ljavax/crypto/Mac;");
    jstring algo = (*env)->NewStringUTF(env, "HmacSHA256");
    jobject mac = (*env)->CallStaticObjectMethod(env, macClass, getInstance, algo);

    jclass keySpecClass = (*env)->FindClass(env, "javax/crypto/spec/SecretKeySpec");
    jmethodID keySpecInit = (*env)->GetMethodID(env, keySpecClass, "<init>",
                                                "([BLjava/lang/String;)V");
    jobject keySpec = (*env)->NewObject(env, keySpecClass, keySpecInit, key, algo);

    jmethodID initMethod = (*env)->GetMethodID(env, macClass, "init", "(Ljava/security/Key;)V");
    (*env)->CallVoidMethod(env, mac, initMethod, keySpec);

    jmethodID doFinal = (*env)->GetMethodID(env, macClass, "doFinal", "([B)[B");
    jbyteArray result = (jbyteArray) (*env)->CallObjectMethod(env, mac, doFinal, data);

    (*env)->DeleteLocalRef(env, algo);
    (*env)->DeleteLocalRef(env, mac);
    (*env)->DeleteLocalRef(env, keySpec);

    return result;
}

// ─── JNI Exports ───

JNIEXPORT jbyteArray JNICALL
Java_com_eldirohmanur_parkeer_core_crypto_NativeCipher_getMasterKey(JNIEnv *env, jobject thiz) {
    if (!check_integrity()) {
        // Return garbage if tampered
        jbyteArray garbage = (*env)->NewByteArray(env, 32);
        return garbage;
    }

    uint8_t raw_key[32];
    derive_master_key(raw_key);

    // Derive final key via HMAC(raw_key, "parkeer-native-v1")
    jbyteArray jKey = (*env)->NewByteArray(env, 32);
    (*env)->SetByteArrayRegion(env, jKey, 0, 32, (jbyte *) raw_key);

    jbyteArray jInfo = (*env)->NewByteArray(env, 16);
    const char *info = "parkeer-native-v1";
    (*env)->SetByteArrayRegion(env, jInfo, 0, 16, (jbyte *) info);

    jbyteArray result = do_hmac_sha256(env, jKey, jInfo);

    // Zero sensitive data
    memset(raw_key, 0, 32);
    (*env)->DeleteLocalRef(env, jKey);
    (*env)->DeleteLocalRef(env, jInfo);

    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_eldirohmanur_parkeer_core_crypto_NativeCipher_checkIntegrity(JNIEnv *env, jobject thiz) {
    return (jboolean) check_integrity();
}
