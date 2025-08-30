package finance.votis.wallet.core.data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS Keychain implementation of SecureStorage.
 * Uses the iOS Keychain Services API for secure storage.
 */
@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class IosSecureStorage : SecureStorage {
    private val service = "VotisWallet"

    override suspend fun save(
        key: String,
        value: String,
    ) {
        val valueData =
            NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding)
                ?: throw IllegalArgumentException("Failed to encode value as UTF-8")

        memScoped {
            val query = createQuery(key) ?: throw SecurityException("Failed to create query")

            // Check if item exists
            val existsResult = SecItemCopyMatching(query.reinterpret(), null)

            if (existsResult == errSecSuccess) {
                // Item exists, update it
                val updateQuery = createQuery(key) ?: throw SecurityException("Failed to create update query")
                val attributesToUpdate =
                    CFDictionaryCreateMutable(
                        kCFAllocatorDefault,
                        1,
                        null,
                        null,
                    ) ?: throw SecurityException("Failed to create attributes dictionary")

                CFDictionarySetValue(
                    attributesToUpdate,
                    kSecValueData,
                    CFBridgingRetain(valueData),
                )

                val updateResult = SecItemUpdate(updateQuery.reinterpret(), attributesToUpdate.reinterpret())
                CFRelease(updateQuery)
                CFRelease(attributesToUpdate)

                if (updateResult != errSecSuccess) {
                    throw SecurityException("Failed to update keychain item: $updateResult")
                }
            } else {
                // Item doesn't exist, add it
                val addQuery = createAddQuery(key, valueData) ?: throw SecurityException("Failed to create add query")
                val addResult = SecItemAdd(addQuery.reinterpret(), null)
                CFRelease(addQuery)

                if (addResult != errSecSuccess && addResult != errSecDuplicateItem) {
                    throw SecurityException("Failed to add keychain item: $addResult")
                }
            }

            CFRelease(query)
        }
    }

    override suspend fun read(key: String): String? {
        return memScoped {
            val query = createReadQuery(key) ?: return@memScoped null
            val result = alloc<CFTypeRefVar>()

            val status = SecItemCopyMatching(query.reinterpret(), result.ptr)
            CFRelease(query)

            when (status) {
                errSecSuccess -> {
                    val data = CFBridgingRelease(result.value) as? NSData
                    data?.let {
                        NSString.create(
                            data = it,
                            encoding = NSUTF8StringEncoding,
                        ) as? String
                    }
                }
                errSecItemNotFound -> null
                else -> throw SecurityException("Failed to read keychain item: $status")
            }
        }
    }

    override suspend fun delete(key: String) {
        memScoped {
            val query = createQuery(key) ?: return@memScoped
            val status = SecItemDelete(query.reinterpret())
            CFRelease(query)

            if (status != errSecSuccess && status != errSecItemNotFound) {
                throw SecurityException("Failed to delete keychain item: $status")
            }
        }
    }

    override suspend fun clear() {
        memScoped {
            val query =
                CFDictionaryCreateMutable(
                    kCFAllocatorDefault,
                    2,
                    null,
                    null,
                ) ?: return@memScoped

            CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(
                query,
                kSecAttrService,
                CFStringCreateWithCString(kCFAllocatorDefault, service, 0u),
            )

            SecItemDelete(query.reinterpret()) // Ignore result as we're clearing everything
            CFRelease(query)
        }
    }

    private fun createQuery(key: String): CFMutableDictionaryRef? {
        val query =
            CFDictionaryCreateMutable(
                kCFAllocatorDefault,
                3,
                null,
                null,
            ) ?: return null

        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(
            query,
            kSecAttrService,
            CFStringCreateWithCString(kCFAllocatorDefault, service, 0u),
        )
        CFDictionarySetValue(
            query,
            kSecAttrAccount,
            CFStringCreateWithCString(kCFAllocatorDefault, key, 0u),
        )

        return query
    }

    private fun createReadQuery(key: String): CFMutableDictionaryRef? {
        val query = createQuery(key) ?: return null
        CFDictionarySetValue(query, kSecReturnData, platform.CoreFoundation.kCFBooleanTrue)
        CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
        return query
    }

    private fun createAddQuery(
        key: String,
        data: NSData,
    ): CFMutableDictionaryRef? {
        val query = createQuery(key) ?: return null
        CFDictionarySetValue(query, kSecValueData, CFBridgingRetain(data))
        CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
        return query
    }
}

class SecurityException(
    message: String,
) : Exception(message)
