package phonebook

import java.io.File
import kotlin.math.sqrt


fun main() {

    val findFile = File("find.txt")
    val directoryFile = File("directory.txt")

    val findLines = findFile.readLines()
    val directoryLines = directoryFile.readLines()

    val query = Query(directoryLines, findLines)


}

class Query(private val directoryLines: List<String>, private val findLines: List<String>) {
    private var sortedLines: MutableList<String> = mutableListOf()
    private var hashTable: HashMap<String, String>
    private val size: Int = findLines.size

    private var linearTime: TimeMap

    private var bubbleSortTime: TimeMap
    private var jumpTime: TimeMap

    private var quickSortTime: TimeMap
    private var binaryTime: TimeMap

    private var creatingTime: TimeMap
    private var hashTime: TimeMap


    init {
        println("Start searching (linear search)...")
        var startTime = System.currentTimeMillis()
        linearSearch()
        var stopTime = System.currentTimeMillis()
        linearTime = TimeMap(stopTime - startTime)
        PrintInfo.search(linearTime)

        println("Start searching (bubble sort + jump search)...")
        startTime = System.currentTimeMillis()
        val isSorted = bubbleSort()
        stopTime = System.currentTimeMillis()
        bubbleSortTime = TimeMap(stopTime - startTime)

        startTime = System.currentTimeMillis()
        if (isSorted) {
            jumpSearch()
        } else {
            linearSearch()
        }
        stopTime = System.currentTimeMillis()
        jumpTime = TimeMap(stopTime - startTime)
        PrintInfo.sortAndSearch(jumpTime, bubbleSortTime)

        println("Start searching (quick sort + binary search)...")
        startTime = System.currentTimeMillis()
        quicksort(directoryLines)
        stopTime = System.currentTimeMillis()
        quickSortTime = TimeMap(stopTime - startTime)

        startTime = System.currentTimeMillis()
        binarySearch()
        stopTime = System.currentTimeMillis()
        binaryTime = TimeMap(stopTime - startTime)
        PrintInfo.sortAndSearch(binaryTime, quickSortTime)

        println("Start searching (hash table)...")
        startTime = System.currentTimeMillis()
        hashTable = createHashTable()
        stopTime = System.currentTimeMillis()
        creatingTime = TimeMap(stopTime - startTime)

        startTime = System.currentTimeMillis()
        hashSearch()
        stopTime = System.currentTimeMillis()
        hashTime = TimeMap(stopTime - startTime)
        PrintInfo.createAndSearch(hashTime, creatingTime)
    }

    private fun linearSearch() {
        var foundLines = 0
        findLines.forEach {
            for (directoryLine in directoryLines) {
                if (directoryLine.contains(it)) {
                    foundLines++
                    break
                }
            }
        }
        printFounded(foundLines)
    }

    private fun bubbleSort(): Boolean {
        val startTime = System.currentTimeMillis()
        sortedLines = directoryLines.toMutableList()
        var swap = true
        while (swap) {
            swap = false
            for (i in 0..sortedLines.size - 2) {
                val time = System.currentTimeMillis() - startTime
                if (time > 10 * linearTime.time) {
                    bubbleSortTime = TimeMap(time)
                    return false
                }
                if (sortedLines[i].withoutNumber() > sortedLines[i + 1].replace(
                        Regex("//d+ "), ""
                    )
                ) {
                    val temp = sortedLines[i]
                    sortedLines[i] = sortedLines[i + 1]
                    sortedLines[i + 1] = temp
                    swap = true
                }
            }
        }
        return true
    }

    private fun jumpSearch() {
        var foundLines = 0
        findLines.forEach {
            if (isFound(sortedLines, it)) foundLines++
        }
        printFounded(foundLines)
    }

    private fun quicksort(items: List<String>): List<String> {
        if (items.count() < 2) {
            return items
        }
        val pivot = items[items.count() / 2].withoutNumber()

        val equal = items.filter { it.withoutNumber() == pivot }
        val less = items.filter { it.withoutNumber() < pivot }
        val greater = items.filter { it.withoutNumber() > pivot }
        return quicksort(less) + equal + quicksort(greater)
    }

    private fun String.withoutNumber() = this.replace(Regex("//d+ "), "")

    private fun binarySearch() {
        var foundLines = 0
        findLines.forEach {
            if (sortedLines.binarySearch(it) != -1) foundLines++
        }
        printFounded(foundLines)
    }


    private fun isFound(list: List<String>, value: String): Boolean {
        val step = sqrt(size.toDouble()).toInt()
        var curr = 1
        while (curr <= size) {
            if (list[curr] == value) return true
            else if (list[curr] > value) {
                var ind = curr - 1
                while (ind > curr - step && ind >= 1) {
                    if (list[ind] == value) return true
                    ind -= 1
                }
                curr += step
            }
        }
        var ind = list.size
        while (ind > curr - step) {
            if (list[ind] == value) return true
            ind -= 1
        }
        return false
    }

    private fun printFounded(foundLines: Int) = print("Found $foundLines / $size entries. ")

    private fun createHashTable(): HashMap<String, String> {
        val records = hashMapOf<String, String>()
        directoryLines.forEach {
            val temp = it.split(" ", limit = 2)
            records[temp[1]] = temp[0]
        }
        return records
    }

    private fun hashSearch() {
        var foundLines = 0
        findLines.forEach {
            if (hashTable[it] != null) foundLines++
        }
        printFounded(foundLines)
    }


}

data class TimeMap(val time: Long) {
    private val minutes: Long = time / 1000 / 60
    private val seconds = time / 1000 % 60
    private val milliseconds = time - minutes * 60000 - seconds * 1000

    override fun toString(): String {
        return "$minutes min. $seconds sec. $milliseconds ms."

    }

    operator fun plus(jumpTime: TimeMap): TimeMap = TimeMap(time + jumpTime.time)
}

object PrintInfo {

    fun search(searchingTime: TimeMap) {
        println("Time taken: $searchingTime\n")
    }

    fun sortAndSearch(searchingTime: TimeMap, sortingTime: TimeMap, isSorted: Boolean = true) {
        var output = "Time taken: ${searchingTime + sortingTime}\nSorting time: $sortingTime "
        if (!isSorted) output += "- STOPPED, moved to linear search."
        output += "Searching time: $searchingTime"
        println(output)
    }

    fun createAndSearch(searchingTime: TimeMap, creatingTime: TimeMap) {
        var output = "Time taken: ${searchingTime + creatingTime}\nCreating time: $creatingTime "
        output += "Searching time: $searchingTime"
        println(output)
    }

}