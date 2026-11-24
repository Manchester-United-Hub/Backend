package backend.manuhub.config;

import backend.manuhub.initializer.StartupInitializerRunner;
import backend.manuhub.match.MatchInitializeService;
import backend.manuhub.season.SeasonInitializeService;
import backend.manuhub.seasonplayer.SeasonPlayerInitializeService;
import backend.manuhub.teamstatistics.TeamStatisticsInitializeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

// 운영 DB에 연결하는 로컬 앱에서 초기화와 예약 작업을 차단하는 설정을 검증한다.
class LocalExecutionConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SchedulingConfig.class, StartupInitializerRunner.class)
            .withBean(SeasonInitializeService.class, () -> mock(SeasonInitializeService.class))
            .withBean(MatchInitializeService.class, () -> mock(MatchInitializeService.class))
            .withBean(SeasonPlayerInitializeService.class, () -> mock(SeasonPlayerInitializeService.class))
            .withBean(TeamStatisticsInitializeService.class, () -> mock(TeamStatisticsInitializeService.class));

    @Test
    @DisplayName("운영 DB 로컬 실행 옵션이 꺼져 있으면 초기화와 예약 작업 빈을 등록하지 않는다")
    void disablesInitializationAndScheduling() {
        runner.withPropertyValues("app.initialization.enabled=false", "app.scheduling.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(StartupInitializerRunner.class);
                    assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                });
    }

    @Test
    @DisplayName("별도 옵션이 없으면 기존 운영 초기화와 스케줄러 동작을 유지한다")
    void preservesExistingDefaults() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(StartupInitializerRunner.class);
            assertThat(context).hasSingleBean(ScheduledAnnotationBeanPostProcessor.class);
        });
    }
}
