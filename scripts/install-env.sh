#!/bin/bash
# MiniClaw 开发环境安装脚本
# 用途：安装 JDK 21 和 Maven 3.9

set -e  # 遇到错误立即退出

echo "========================================="
echo "MiniClaw 开发环境安装脚本"
echo "========================================="

# 检测操作系统
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    echo "无法检测操作系统"
    exit 1
fi

echo "检测到操作系统: $OS"

# 1. 安装 JDK 21
install_jdk() {
    echo ""
    echo ">>> 步骤 1/2: 安装 JDK 21"
    echo ""

    # 检查是否已安装
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            echo "✓ JDK $JAVA_VERSION 已安装"
            java -version
            return 0
        fi
    fi

    echo "开始安装 JDK 21..."

    case $OS in
        ubuntu|debian)
            apt-get update
            apt-get install -y openjdk-21-jdk
            ;;
        centos|rhel|rocky|almalinux|opencloudos)
            yum install -y java-21-openjdk-devel
            ;;
        *)
            echo "不支持的操作系统: $OS"
            echo "请手动安装 JDK 21+"
            exit 1
            ;;
    esac

    # 验证安装
    if command -v java &> /dev/null; then
        echo "✓ JDK 21 安装成功"
        java -version
    else
        echo "✗ JDK 21 安装失败"
        exit 1
    fi
}

# 2. 安装 Maven 3.9
install_maven() {
    echo ""
    echo ">>> 步骤 2/2: 安装 Maven 3.9"
    echo ""

    # 检查是否已安装
    if command -v mvn &> /dev/null; then
        MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)
        echo "✓ Maven 已安装: $MAVEN_VERSION"
        mvn -version
        return 0
    fi

    echo "开始安装 Maven 3.9..."

    MAVEN_VERSION="3.9.9"
    MAVEN_URL="https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    MAVEN_HOME="/usr/local/apache-maven-${MAVEN_VERSION}"

    # 下载 Maven
    cd /tmp
    wget -q $MAVEN_URL -O apache-maven.tar.gz

    # 解压到 /usr/local
    tar -xzf apache-maven.tar.gz -C /usr/local/

    # 配置环境变量
    echo "" >> /etc/profile
    echo "# Maven" >> /etc/profile
    echo "export MAVEN_HOME=$MAVEN_HOME" >> /etc/profile
    echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >> /etc/profile

    # 立即生效
    export MAVEN_HOME=$MAVEN_HOME
    export PATH=$MAVEN_HOME/bin:$PATH

    # 验证安装
    if command -v mvn &> /dev/null; then
        echo "✓ Maven $MAVEN_VERSION 安装成功"
        mvn -version
    else
        echo "✗ Maven 安装失败"
        exit 1
    fi

    # 清理临时文件
    rm -f apache-maven.tar.gz
}

# 3. 验证环境
verify_environment() {
    echo ""
    echo "========================================="
    echo "环境验证"
    echo "========================================="

    echo ""
    echo "Java 版本:"
    java -version

    echo ""
    echo "Maven 版本:"
    mvn -version

    echo ""
    echo "✓ 环境安装完成！"
    echo ""
    echo "下一步："
    echo "  cd /root/.openclaw/workspace/miniclaw-learn"
    echo "  mvn spring-boot:run"
}

# 主流程
main() {
    install_jdk
    install_maven
    verify_environment
}

main
