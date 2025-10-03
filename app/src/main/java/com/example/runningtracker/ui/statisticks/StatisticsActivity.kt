package com.example.runningtracker.ui.statistics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.runningtracker.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Активность, отображающая статистику пробежек.
 * Использует ViewPager2 с TabLayout для переключения между различными периодами статистики:
 * - День
 * - Неделя
 * - Месяц
 */
class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Находим элементы интерфейса
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: androidx.viewpager2.widget.ViewPager2 = findViewById(R.id.viewPager)

        // Создаём адаптер для ViewPager2
        val adapter = StatsPagerAdapter(this)
        viewPager.adapter = adapter

        // Заголовки для вкладок
        val titles = arrayOf("День", "Неделя", "Месяц")

        // Связываем TabLayout и ViewPager2 с помощью TabLayoutMediator
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Устанавливаем текст для каждой вкладки
            tab.text = titles[position]
        }.attach() // Применяем связь
    }

    /**
     * Адаптер для ViewPager2, который управляет фрагментами статистики.
     */
    class StatsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3 // Количество вкладок

        override fun createFragment(position: Int): Fragment {
            // Возвращаем соответствующий фрагмент в зависимости от позиции вкладки
            return when (position) {
                0 -> DailyStatsFragment()   // Фрагмент для дневной статистики
                1 -> WeeklyStatsFragment()  // Фрагмент для недельной статистики
                2 -> MonthlyStatsFragment() // Фрагмент для месячной статистики
                else -> WeeklyStatsFragment() // Резервный вариант (не должен сработать при 3 вкладках)
            }
        }
    }
}